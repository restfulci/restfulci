package restfulci.job.slave.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.error.YAMLException;
import org.zeroturnaround.zip.ZipUtil;

import com.github.dockerjava.api.exception.BadRequestException;
import com.github.dockerjava.api.exception.NotFoundException;

import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;
import restfulci.job.shared.dao.MinioRepository;
import restfulci.job.shared.dao.RemoteGitRepository;
import restfulci.job.shared.dao.RunRepository;
import restfulci.job.shared.domain.FreestyleJobBean;
import restfulci.job.shared.domain.FreestyleRunBean;
import restfulci.job.shared.domain.GitRunBean;
import restfulci.job.shared.domain.InputBean;
import restfulci.job.shared.domain.RunBean;
import restfulci.job.shared.domain.RunConfigBean;
import restfulci.job.shared.domain.RunMessageBean;
import restfulci.job.shared.domain.RunResultBean;
import restfulci.job.shared.domain.RunStatus;
import restfulci.job.slave.dto.RunCommandDTO;
import restfulci.job.slave.exec.DockerExec;

@Slf4j
@Service
public class DockerRunServiceImpl implements DockerRunService {
	
	@Autowired private DockerExec dockerExec;
	
	@Autowired private RunRepository runRepository;
	@Autowired private RemoteGitRepository remoteGitRepository;
	@Autowired private MinioRepository minioRepository;
	
	private final String networkName = "restfulci-net";
	private final String mainContainerName = "restfulci-main";

	private RunBean getRun(Integer runId) throws IOException {
		
		Optional<RunBean> runs = runRepository.findById(runId);
		if (runs.isPresent()) {
			return runs.get();
		}
		else {
			throw new IOException();
		}
	}
	
	@Override
	public void runByMessage(RunMessageBean runMessage) throws InterruptedException, IOException {
		
		RunBean run = getRun(runMessage.getRunId());
		
		dockerExec.createNetworkIfNotExist(networkName);
		
		if (run instanceof FreestyleRunBean) {
			FreestyleRunBean freestyleRun = (FreestyleRunBean)run;
			runFreestyleJob(freestyleRun);
		}
		else if (run instanceof GitRunBean) {
			GitRunBean gitRun = (GitRunBean)run;
			runGitJob(gitRun);
		}
		else {
			throw new IOException("Input run with wrong type");
		}
		
		run.setCompleteAt(new Date());
		runRepository.saveAndFlush(run);
	}
	
	private Map<String, String> getEnvVars(RunBean run) {	
		Map<String, String> envVars = new HashMap<String, String>();
		updateWithInputs(envVars, run);
		return envVars;
	}
	
	private Map<String, String> getEnvVars(RunConfigBean runConfig, RunBean run) {
		Map<String, String> envVars = runConfig.getExecutor().getEnvironment();
		updateWithInputs(envVars, run);
		return envVars;
	}
	
	private void updateWithInputs(Map<String, String> envVars, RunBean run) {
		for (InputBean input : run.getInputs()) {
			envVars.put(input.getName(), input.getValue());
		}
	}
	
	private void runFreestyleJob(FreestyleRunBean run) throws InterruptedException {
		
		FreestyleJobBean job = run.getJob();
		
		try {
			dockerExec.pullImage(job.getDockerImage());
		}
		catch (NotFoundException e) {
			log.info("Freestyle job pulling image error: {}", e.getMessage());
			run.setStatus(RunStatus.FAIL);
			run.setErrorMessage("Pulling image error: \n"+e.getMessage());
			return;
		}
		
		try {
			RunCommandDTO runDTO = dockerExec.runCommand(
					job.getDockerImage(), 
					mainContainerName,
					networkName,
					Arrays.asList(job.getCommand()),
					getEnvVars(run),
					new HashMap<RunConfigBean.RunConfigResultBean, File>(),
					run.getDefaultRunOutputObjectReferral());
			
			runDTO.updateRunBean(run);
		}
		catch (BadRequestException e) {
			log.info("Freestyle job invalid command: {}", e.getMessage());
			run.setStatus(RunStatus.FAIL);
			run.setErrorMessage("Invalid command: \n"+e.getMessage());
		}
		
		/*
		 * TODO:
		 * Freestyle job should be able to very easily support saving result as well.
		 */
	}
	
	private void runGitJob(GitRunBean run) throws InterruptedException, IOException {
		
		/*
		 * Steps:
		 * (1) Create temporary local folder.
		 * (2) Clone (by branch name or commit SHA) single commit into the local folder.
		 * (3) Find the config file in local folder and generate `RunConfigBean`.
		 * (4) Build image, run sidecars, run job, kill sidecars (based on config file and the contents in git clone).
		 * (5) Clean up docker container (TODO).
		 * (6) Clean up the temporary local folder (TODO).
		 */
		Path localRepoPath = Files.createTempDirectory("local-repo");
		
		try {
			remoteGitRepository.copyToLocal(run, localRepoPath);
		}
		catch (IOException e) {
			log.info("Git clone fails: {}", e.getMessage());
			run.setStatus(RunStatus.FAIL);
			run.setErrorMessage("Git clone fails: \n"+e.getMessage());
			return;
			/*
			 * TODO:
			 * Should we define error code, error short name, and
			 * then pass in error message?
			 */
		}
		
		RunConfigBean runConfig;
		try {
			runConfig = remoteGitRepository.getConfigFromFilepath(run, localRepoPath);
		}
		catch (IOException e) {
			log.info("Git job error getting config file: {}", e.getMessage());
			run.setStatus(RunStatus.FAIL);
			run.setErrorMessage("Error getting config file: \n"+e.getMessage());
			return;
		}
		catch (YAMLException e) {
			log.info("Git job config YAML parsing error: {}", e.getMessage());
			run.setStatus(RunStatus.FAIL);
			run.setErrorMessage("Config YAML parsing error: \n"+e.getMessage());
			return;
		}
		
		try {
			log.info("Save configuration file");
			Path configFilepath = remoteGitRepository.getConfigFilepath(run, localRepoPath);
			String runConfigurationObjectReferral = minioRepository.putRunConfigurationAndReturnObjectName(
					new FileInputStream(configFilepath.toFile()), run.getDefaultRunConfigurationObjectReferral());
			run.setRunConfigurationObjectReferral(runConfigurationObjectReferral);
		} 
		catch (MinioException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*
		 * For Docker desktop on Mac, need to edit `Library/Group\ Containers/group.com.docker/settings.json`
		 * to add `/var/folders`. Otherwise unit test (execute on Mac / not inside of container) error:
		 * > com.github.dockerjava.api.exception.InternalServerErrorException: Mounts denied: 
		 * > The path /var/folders/kp/fz7j3bln4m11rc197xrj4dvc0000gq/T/junit5099823135583804007/results
		 * > is not shared from OS X and is not known to Docker.
		 * > You can configure shared paths from Docker -> Preferences... -> File Sharing.
		 * > See https://docs.docker.com/docker-for-mac/osxfs/#namespaces for more info.
		 * 
		 * Notice that both `@TempDir` and `Files.createTempDirectory` 
		 * are using `/var/folders` (Mac) or `/tmp` (linux) to save the file.
		 */
		Map<RunConfigBean.RunConfigResultBean, File> mounts = new HashMap<RunConfigBean.RunConfigResultBean, File>();
		for (RunConfigBean.RunConfigResultBean result : runConfig.getResults()) {
			
			/*
			 * Note:
			 * 
			 * In actual run inside of the container, this temporary folder created
			 * by `Files.createTempDirectory` is created inside of the docker container.
			 * As docker container uses Alpine, it is under `/tmp`.
			 * 
			 * However, as for container docker we pass in socket `-v /var/run/docker.sock:/var/run/docker.sock`
			 * when we use volume mount to pass the result out inside of `DockerExec`,
			 * the result will be sent to `/tmp` in hosting machine where 
			 * the docker application is running.
			 * 
			 * Therefore, to actually see the result/docker volume change in the
			 * Java application inside of the container, we'll need to not only
			 * `-v /var/run/docker.sock:/var/run/docker.sock` but also `-v /tmp:/tmp`.
			 * Otherwise we'll see empty content under this folder:
			 * > slave-agent_1  | 2020-04-19 04:05:47.812  INFO 1 --- [in-0.runqueue-1] r.slave.service.DockerRunServiceImpl     : Zip run result: /result
			 * > slave-agent_1  | Content: [] temperarily saved at /tmp/result-4099381032373732624
			 * and zip error:
			 * > nested exception is org.zeroturnaround.zip.ZipException: Given directory 
			 * > '/tmp/result-966542229242736184' doesn't contain any files!
			 * 
			 * When this error raises, Spring will reload the message and retry
			 * multiple (~3) times before showing the error.
			 * 
			 * TODO:
			 * Consider using a directory other than the default of `Files.createTempDirectory`,
			 * so we don't need to volume mount `/tmp` which is used by a lot of other
			 * applications both in host machine and inside of the container.
			 */
			mounts.put(result, Files.createTempDirectory("result-").toFile());
		}
		
		/*
		 * TODO:
		 * If sidecars can support build from Dockerfile, we'll need to build sidecars as well.
		 */
		
		List<String> sidecarIds = new ArrayList<String>();	
		try {
			for (RunConfigBean.RunConfigSidecarBean sidecar : runConfig.getSidecars()) {
				
				try {
					dockerExec.pullImage(sidecar.getImage());
				}
				catch (NotFoundException e) {
					log.info("Git job pulling sidecar image error: {}", e.getMessage());
					run.setStatus(RunStatus.FAIL);
					run.setErrorMessage("Pulling sidecar image error: \n"+e.getMessage());
					return;
				}
				
				sidecarIds.add(
						dockerExec.createSidecar(
								sidecar.getImage(), 
								sidecar.getName(), 
								networkName,
								sidecar.getCommand(),
								sidecar.getEnvironment()));
			}
			
			String imageId;
			if (runConfig.getExecutor().getImage() != null) {
				imageId = runConfig.getExecutor().getImage();
				try {
					dockerExec.pullImage(imageId);
				}
				catch (NotFoundException e) {
					log.info("Git job pulling main image error: {}", e.getMessage());
					run.setStatus(RunStatus.FAIL);
					run.setErrorMessage("Pulling main image error: \n"+e.getMessage());
					return;
				}
			}
			else {
				try {
					imageId = dockerExec.buildImageAndGetId(localRepoPath, runConfig);
				}
				catch (BadRequestException | IllegalArgumentException e) {
					log.info("Git job docker build error: {}", e.getMessage());
					run.setStatus(RunStatus.FAIL);
					run.setErrorMessage("Docker build error: \n"+e.getMessage());
					return;
				}
			}
			
			try {
				RunCommandDTO runDTO = dockerExec.runCommand(
						imageId, 
						mainContainerName,
						networkName,
						runConfig.getCommand(), 
						getEnvVars(runConfig, run),
						mounts,
						run.getDefaultRunOutputObjectReferral());
				
				runDTO.updateRunBean(run);
			}
			catch (BadRequestException e) {
				log.info("Git job invalid command: {}", e.getMessage());
				run.setStatus(RunStatus.FAIL);
				run.setErrorMessage("Invalid command: \n"+e.getMessage());
			}
		}
		finally {
			for (String sidecarId : sidecarIds) {
				dockerExec.killSidecar(sidecarId);
			}
		}
		
		for (Map.Entry<RunConfigBean.RunConfigResultBean, File> entry : mounts.entrySet()) {
			
			log.info(
					"Zip run result: {}\n"
					+ "Content: {} temperarily saved at {}", 
					entry.getKey().getPath(), 
					Arrays.toString(entry.getValue().list()), 
					entry.getValue());
			File zipFile = Files.createTempFile("result", ".zip").toFile();
			ZipUtil.pack(entry.getValue(), zipFile);
			
			RunResultBean runResult = new RunResultBean();
			
			try {
				runResult.setContainerPath(entry.getKey().getPath());
				runResult.setType(entry.getKey().getType());
				String runResultObjectReferral = minioRepository.putRunResultAndReturnObjectName(
						new FileInputStream(zipFile), runResult.getDefaultObjectReferral());
				runResult.setObjectReferral(runResultObjectReferral);
			} 
			catch (MinioException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.info("Save run result: {}", entry.getKey().getPath());
			runResult.setRun(run);
			/*
			 * No need to do it, as we are saving by `runResultRepository` rather
			 * than `runRepository`.
			 * 
			 * This also help us to avoid the need of loading all results belong
			 * to the same run. 
			 */
			run.getRunResults().add(runResult);
		}
	}
}
