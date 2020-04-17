package restfulci.slave.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
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
import restfulci.shared.dao.RunResultRepository;
import restfulci.shared.domain.FreestyleJobBean;
import restfulci.shared.domain.FreestyleRunBean;
import restfulci.shared.domain.GitRunBean;
import restfulci.shared.domain.RunBean;
import restfulci.shared.domain.RunConfigBean;
import restfulci.shared.domain.RunMessageBean;
import restfulci.shared.domain.RunResultBean;
import restfulci.slave.exec.DockerExec;

@Slf4j
@Service
public class DockerRunServiceImpl implements DockerRunService {
	
	@Autowired private DockerExec dockerExec;
	
	@Autowired private RunRepository runRepository;
	@Autowired private RunResultRepository runResultRepository;
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
	}
	
	private void runFreestyleJob(FreestyleRunBean run) throws InterruptedException {
		FreestyleJobBean job = run.getJob();
		dockerExec.pullImage(job.getDockerImage());
		dockerExec.runCommand(
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
			runRepository.saveAndFlush(run);
		} 
		catch (MinioException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		RunConfigBean runConfig = remoteGitRepository.getConfigFromFilepath(run, localRepoPath);
		
		/*
		 * For Docker desktop on Mac, need to edit `Library/Group\ Containers/group.com.docker/settings.json`
		 * to add `/var/folders`. Otherwise error:
		 * > com.github.dockerjava.api.exception.InternalServerErrorException: Mounts denied: 
		 * > The path /var/folders/kp/fz7j3bln4m11rc197xrj4dvc0000gq/T/junit5099823135583804007/results
		 * > is not shared from OS X and is not known to Docker.
		 * > You can configure shared paths from Docker -> Preferences... -> File Sharing.
		 * > See https://docs.docker.com/docker-for-mac/osxfs/#namespaces for more info.
		 * 
		 * Notice that both `@TempDir` and `Files.createTempDirectory` 
		 * are using `/var/folders` to save the file.
		 */
		Map<RunConfigBean.RunConfigResultBean, File> mounts = new HashMap<RunConfigBean.RunConfigResultBean, File>();
		for (RunConfigBean.RunConfigResultBean result : runConfig.getResults()) {
			mounts.put(result, Files.createTempDirectory("result").toFile());
		}
		
		if (runConfig.getEnvironment().getImage() != null) {
			dockerExec.pullImage(runConfig.getEnvironment().getImage());
			dockerExec.runCommand(run, runConfig.getEnvironment().getImage(), runConfig.getCommand(), mounts);
		}
		else {
			String imageId = dockerExec.buildImageAndGetId(localRepoPath, runConfig);
			dockerExec.runCommand(run, imageId, runConfig.getCommand(), mounts);
		}
		
		for (Map.Entry<RunConfigBean.RunConfigResultBean, File> entry : mounts.entrySet()) {
			
			log.info("Zip run result: "+entry.getKey().getPath());
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
			
			/*
			 * TODO:
			 * Currently there's a bug that /jobs/{}/runs/{} always give
			 * [] runResult. Looks like this line has never been arrived.
			 * 
			 * It maybe because of the multiple runRepository.saveAndFlush(),
			 * and the 2nd execution's initial saveAndFlush() reset the 
			 * original runResults to [].
			 * Or maybe spring terminated this thread after RabbitMQ message
			 * get delivered and it starts a new one.
			 * 
slave-agent_1  | 2020-04-17 04:34:55.362  INFO 1 --- [in-0.runqueue-1] r.slave.service.DockerRunServiceImpl     : Save configuration file
slave-agent_1  | 2020-04-17 04:34:55.416  INFO 1 --- [in-0.runqueue-1] restfulci.slave.exec.DockerExecImpl      : Build image from context path ./python-pytest and Dockerfile path ./Dockerfile
slave-agent_1  | 2020-04-17 04:34:59.997  INFO 1 --- [in-0.runqueue-1] restfulci.slave.exec.DockerExecImpl      : Execute command [pytest] in docker image: c244baf6a8cf
slave-agent_1  | 2020-04-17 04:35:00.863  INFO 1 --- [in-0.runqueue-1] r.slave.service.DockerRunServiceImpl     : Zip run result: /code/test-results
slave-agent_1  | 2020-04-17 04:35:02.278  INFO 1 --- [in-0.runqueue-1] r.slave.service.DockerRunServiceImpl     : Save configuration file
slave-agent_1  | 2020-04-17 04:35:02.356  INFO 1 --- [in-0.runqueue-1] restfulci.slave.exec.DockerExecImpl      : Build image from context path ./python-pytest and Dockerfile path ./Dockerfile
slave-agent_1  | 2020-04-17 04:35:06.884  INFO 1 --- [in-0.runqueue-1] restfulci.slave.exec.DockerExecImpl      : Execute command [pytest] in docker image: 1f916286902b
slave-agent_1  | 2020-04-17 04:35:09.730 ERROR 1 --- [in-0.runqueue-1] o.s.integration.handler.LoggingHandler   : org.springframework.messaging.Message
			 */
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
			runResultRepository.saveAndFlush(runResult);
		}
	}
}
