package restfulci.slave.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zeroturnaround.zip.ZipUtil;

import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;
import restfulci.shared.dao.MinioRepository;
import restfulci.shared.dao.RemoteGitRepository;
import restfulci.shared.dao.RunRepository;
import restfulci.shared.domain.FreestyleJobBean;
import restfulci.shared.domain.FreestyleRunBean;
import restfulci.shared.domain.GitRunBean;
import restfulci.shared.domain.RunBean;
import restfulci.shared.domain.RunConfigBean;
import restfulci.shared.domain.RunMessageBean;
import restfulci.shared.domain.RunPhase;
import restfulci.shared.domain.RunResultBean;
import restfulci.slave.exec.DockerExec;

@Slf4j
@Service
public class DockerRunServiceImpl implements DockerRunService {
	
	@Autowired private DockerExec dockerExec;
	
	@Autowired private RunRepository runRepository;
	@Autowired private RemoteGitRepository remoteGitRepository;
	@Autowired private MinioRepository minioRepository;

	@Override
	public void runByMessage(RunMessageBean runMessage) throws InterruptedException, IOException {
		
		RunBean run = runRepository.findById(runMessage.getRunId()).get();
		
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
		
		run.setPhase(RunPhase.COMPLETE);
		run.setCompleteAt(new Date());
		runRepository.saveAndFlush(run);
	}
	
	private void runFreestyleJob(FreestyleRunBean run) throws InterruptedException {
		FreestyleJobBean job = run.getJob();
		dockerExec.pullImage(job.getDockerImage());
		dockerExec.runCommandAndUpdateRunBean(
				run, job.getDockerImage(), 
				Arrays.asList(job.getCommand()), 
				new HashMap<RunConfigBean.RunConfigResultBean, File>());
		
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
		 * (4) Build image, run job based on config file and the contents in git clone.
		 * (5) Clean up docker container (TODO).
		 * (6) Clean up the temporary local folder (TODO).
		 */
		Path localRepoPath = Files.createTempDirectory("local-repo");
		remoteGitRepository.copyToLocal(run, localRepoPath);
		
		try {
			log.info("Save configuration file");
			Path configFilepath = remoteGitRepository.getConfigFilepath(run, localRepoPath);
			minioRepository.putRunConfigurationAndUpdateRunBean(run, new FileInputStream(configFilepath.toFile()));
		} 
		catch (MinioException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		RunConfigBean runConfig = remoteGitRepository.getConfigFromFilepath(run, localRepoPath);
		
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
			 */
			mounts.put(result, Files.createTempDirectory("result-").toFile());
		}
		
		if (runConfig.getEnvironment().getImage() != null) {
			dockerExec.pullImage(runConfig.getEnvironment().getImage());
			dockerExec.runCommandAndUpdateRunBean(run, runConfig.getEnvironment().getImage(), runConfig.getCommand(), mounts);
		}
		else {
			String imageId = dockerExec.buildImageAndGetId(localRepoPath, runConfig);
			dockerExec.runCommandAndUpdateRunBean(run, imageId, runConfig.getCommand(), mounts);
		}
		
		for (Map.Entry<RunConfigBean.RunConfigResultBean, File> entry : mounts.entrySet()) {
			
			log.info("Zip run result: "+entry.getKey().getPath()+"\n"
					+"Content: "+Arrays.toString(entry.getValue().list())
					+" temperarily saved at "+entry.getValue());
			File zipFile = Files.createTempFile("result", ".zip").toFile();
			ZipUtil.pack(entry.getValue(), zipFile);
			
			RunResultBean runResult = new RunResultBean();
			
			try {
				runResult.setContainerPath(entry.getKey().getPath());
				runResult.setType(entry.getKey().getType());
				minioRepository.putRunResultAndUpdateRunResultBean(
						runResult, new FileInputStream(zipFile));
			} 
			catch (MinioException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.info("Save run result: "+entry.getKey().getPath());
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
