package restfulci.slave.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.BuildImageResultCallback;
////import com.github.dockerjava.utils.LogContainerTestCallback;
//import com.github.dockerjava.api.async.ResultCallback;
//import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.github.dockerjava.core.command.WaitContainerResultCallback;

import restfulci.shared.dao.RemoteGitRepository;
import restfulci.shared.dao.RunRepository;
import restfulci.shared.domain.DockerRunCmdResultBean;
import restfulci.shared.domain.FreestyleJobBean;
import restfulci.shared.domain.GitRunBean;
import restfulci.shared.domain.RunBean;
import restfulci.shared.domain.RunConfigBean;
import restfulci.shared.domain.RunMessageBean;
import restfulci.slave.config.bean.DockerDaemon;

@Service
public class DockerRunServiceImpl implements DockerRunService {
	
	@Autowired private DockerDaemon dockerDaemon;
	
	@Autowired private RunRepository runRepository;
	@Autowired private RemoteGitRepository remoteGitRepository;

	@Override
	public void executeRun(RunMessageBean runMessage) throws InterruptedException, IOException {
		
		RunBean run = runRepository.findById(runMessage.getRunId()).get();
		
		/*
		 * TODO: 
		 * Should have a more unified way to decide what type the job/run is.
		 * e.g., create a `FreestyleRunBean` even if its identical to `RunBean`.
		 */
		if (run.getJob() instanceof FreestyleJobBean) {
			FreestyleJobBean freestyleJob = (FreestyleJobBean)run.getJob();
			DockerRunCmdResultBean result = runFreestyleJob(freestyleJob);
			System.out.println(result);
		}
		else if (run instanceof GitRunBean) {
			GitRunBean gitRun = (GitRunBean)run;
			DockerRunCmdResultBean result = runGitJob(gitRun);
			System.out.println(result);
		}
	}
	
	/*
	 * TODO:
	 * Input as run rather then job.
	 */
	@Override
	public DockerRunCmdResultBean runFreestyleJob(FreestyleJobBean job) throws InterruptedException {
		return runCommand(job.getDockerImage(), Arrays.asList(job.getCommand()));
	}
	
	@Override
	public DockerRunCmdResultBean runGitJob(GitRunBean run) throws InterruptedException, IOException {
		
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
		RunConfigBean runConfig = remoteGitRepository.getConfigFromFilepath(run, localRepoPath);
		
		DockerClient dockerClient = getDockerClient();
		
		File dockerfile = localRepoPath
				.resolve(runConfig.getEnvironment().getBuild().getContext())
				.resolve(runConfig.getEnvironment().getBuild().getDockerfile()).toFile();
		
		/*
		 * TODO:
		 * Use context.
		 */
		
		/*
		 * https://github.com/docker-java/docker-java/blob/3.1.5/src/test/java/com/github/dockerjava/cmd/BuildImageCmdIT.java
		 */
		String imageId = dockerClient.buildImageCmd(dockerfile)
	            .withNoCache(true)
	            .exec(new BuildImageResultCallback())
	            .awaitImageId();
		
		return runCommand(imageId, runConfig.getCommand());
	}

	@Override
	public DockerRunCmdResultBean runCommand(String image, List<String> command) throws InterruptedException {
		DockerClient dockerClient = getDockerClient();
		
		/*
		 * TODO:
		 * This may not work if the image is not existing in the docker cache. Observe it
		 * on CircleCI:
		 * - Without `docker pull busybox` in command line test fails: https://circleci.com/gh/restfulci/restfulci/72
		 * - With `docker pull busybox` test passed: https://circleci.com/gh/restfulci/restfulci/76
		 * To be confirmed later.
		 */
		CreateContainerResponse container = dockerClient.createContainerCmd(image)
				.withCmd(command)
				.exec();
		
		int timestamp = (int) (System.currentTimeMillis() / 1000);
		dockerClient.startContainerCmd(container.getId()).exec();
		
		DockerRunCmdResultBean result = new DockerRunCmdResultBean();

		int exitCode = dockerClient.waitContainerCmd(container.getId())
				.exec(new WaitContainerResultCallback())
				.awaitStatusCode();
		result.setExitCode(exitCode);
		
		LogContainerCallbackWrapper loggingCallback = new LogContainerCallbackWrapper();
		dockerClient.logContainerCmd(container.getId())
				.withStdErr(true)
				.withStdOut(true)
				.withSince(timestamp)
				.exec(loggingCallback);
		loggingCallback.awaitCompletion();
		result.setOutput(loggingCallback.toString());
		
		return result;
	}
	
	/*
	 * TODO:
	 * Move to DockerDaemon or config files.
	 */
	private DockerClient getDockerClient() {
		DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
//				.withDockerHost(dockerDaemon.getDockerHost())
				.build();
		DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();
		return dockerClient;
	}
	
	/*
	 * Copyright (c) docker-java
	 * https://github.com/docker-java/docker-java/blob/3.1.5/src/test/java/com/github/dockerjava/utils/LogContainerTestCallback.java#L3
	 */
	private class LogContainerCallbackWrapper extends LogContainerResultCallback {
		protected final StringBuffer log = new StringBuffer();

		List<Frame> collectedFrames = new ArrayList<Frame>();

		boolean collectFrames = false;

		public LogContainerCallbackWrapper() {
			this(false);
		}

		public LogContainerCallbackWrapper(boolean collectFrames) {
			this.collectFrames = collectFrames;
		}

		@Override
		public void onNext(Frame frame) {
			if (collectFrames) collectedFrames.add(frame);
			log.append(new String(frame.getPayload()));
		}

		@Override
		public String toString() {
			return log.toString();
		}
	}
}
