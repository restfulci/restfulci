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
			dockerExec.runCommandAndUpdateRunBean(run, runConfig.getEnvironment().getImage(), runConfig.getCommand(), mounts);
		}
		else {
			String imageId = dockerExec.buildImageAndGetId(localRepoPath, runConfig);
			dockerExec.runCommandAndUpdateRunBean(run, imageId, runConfig.getCommand(), mounts);
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
			 * Currently for complicated job RabbitMQ message will be delivered multiple
			 * times, and it finally will error out (error message not consistent) as our
			 * job is not idempotent/may contain race conditions.
slave-agent_1  | 2020-04-18 03:57:27.724  INFO 1 --- [in-0.runqueue-1] r.slave.service.DockerRunServiceImpl     : Save configuration file
slave-agent_1  | 2020-04-18 03:57:27.774  INFO 1 --- [in-0.runqueue-1] restfulci.slave.exec.DockerExecImpl      : Build image from context path ./python-pytest and Dockerfile path ./Dockerfile
slave-agent_1  | 2020-04-18 03:57:33.197  INFO 1 --- [in-0.runqueue-1] restfulci.slave.exec.DockerExecImpl      : Execute command [pytest && ls /code/test-results] in docker image: 59c29fbabdb9
slave-agent_1  | 2020-04-18 03:57:34.944  INFO 1 --- [in-0.runqueue-1] r.slave.service.DockerRunServiceImpl     : Save configuration file
slave-agent_1  | 2020-04-18 03:57:34.993  INFO 1 --- [in-0.runqueue-1] restfulci.slave.exec.DockerExecImpl      : Build image from context path ./python-pytest and Dockerfile path ./Dockerfile
slave-agent_1  | 2020-04-18 03:57:39.874  INFO 1 --- [in-0.runqueue-1] restfulci.slave.exec.DockerExecImpl      : Execute command [pytest && ls /code/test-results] in docker image: 5fc22554c7a1
slave-agent_1  | 2020-04-18 03:57:42.650  INFO 1 --- [in-0.runqueue-1] r.slave.service.DockerRunServiceImpl     : Save configuration file
slave-agent_1  | 2020-04-18 03:57:42.704  INFO 1 --- [in-0.runqueue-1] restfulci.slave.exec.DockerExecImpl      : Build image from context path ./python-pytest and Dockerfile path ./Dockerfile
slave-agent_1  | 2020-04-18 03:57:47.400  INFO 1 --- [in-0.runqueue-1] restfulci.slave.exec.DockerExecImpl      : Execute command [pytest && ls /code/test-results] in docker image: bff5ead19919
slave-agent_1  | 2020-04-18 03:57:47.855 ERROR 1 --- [in-0.runqueue-1] o.s.integration.handler.LoggingHandler   : org.springframework.messaging.MessageHandlingException: error occurred during processing message in 'MethodInvokingMessageProcessor' [org.springframework.integration.handler.MethodInvokingMessageProcessor@68b0fcde]; nested exception is com.github.dockerjava.api.exception.BadRequestException: OCI runtime create failed: container_linux.go:349: starting container process caused "exec: \"pytest && ls /code/test-results\": stat pytest && ls /code/test-results: no such file or directory": unknown, failedMessage=GenericMessage [payload=byte[32], headers={amqp_receivedDeliveryMode=PERSISTENT, amqp_receivedExchange=, amqp_deliveryTag=4, deliveryAttempt=3, amqp_consumerQueue=executeRun-in-0.runqueue, amqp_redelivered=false, amqp_receivedRoutingKey=executeRun-in-0.runqueue, amqp_contentEncoding=UTF-8, id=97078da0-4e29-17f8-8277-d79403277890, amqp_consumerTag=amq.ctag-8uWQnIZWuQU5ivUYfDOntA, sourceData=(Body:'{
slave-agent_1  |   "jobId" : 5,
slave-agent_1  |   "runId" : 5
slave-agent_1  | }' MessageProperties [headers={}, contentType=text/plain, contentEncoding=UTF-8, contentLength=0, receivedDeliveryMode=PERSISTENT, priority=0, redelivered=false, receivedExchange=, receivedRoutingKey=executeRun-in-0.runqueue, deliveryTag=4, consumerTag=amq.ctag-8uWQnIZWuQU5ivUYfDOntA, consumerQueue=executeRun-in-0.runqueue]), contentType=text/plain, timestamp=1587182247376}]
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
		}
	}
}
