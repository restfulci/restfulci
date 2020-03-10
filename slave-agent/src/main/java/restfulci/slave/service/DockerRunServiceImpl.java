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
import com.github.dockerjava.core.command.BuildImageResultCallback;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.github.dockerjava.core.command.WaitContainerResultCallback;

import restfulci.shared.dao.RemoteGitRepository;
import restfulci.shared.dao.RunRepository;
import restfulci.shared.domain.DockerRunCmdResultBean;
import restfulci.shared.domain.FreestyleJobBean;
import restfulci.shared.domain.FreestyleRunBean;
import restfulci.shared.domain.GitRunBean;
import restfulci.shared.domain.RunBean;
import restfulci.shared.domain.RunConfigBean;
import restfulci.shared.domain.RunMessageBean;

@Service
public class DockerRunServiceImpl implements DockerRunService {
	
	@Autowired private DockerClient dockerClient;
	
	@Autowired private RunRepository runRepository;
	@Autowired private RemoteGitRepository remoteGitRepository;

	@Override
	public void executeRun(RunMessageBean runMessage) throws InterruptedException, IOException {
		
		RunBean run = runRepository.findById(runMessage.getRunId()).get();
		
		if (run instanceof FreestyleRunBean) {
			FreestyleRunBean freestyleRun = (FreestyleRunBean)run;
			DockerRunCmdResultBean result = runFreestyleJob(freestyleRun);
			System.out.println(result);
		}
		else if (run instanceof GitRunBean) {
			GitRunBean gitRun = (GitRunBean)run;
			DockerRunCmdResultBean result = runGitJob(gitRun);
			System.out.println(result);
		}
		else {
			throw new IOException("Input run with wrong type");
		}
	}
	
	@Override
	public DockerRunCmdResultBean runFreestyleJob(FreestyleRunBean run) throws InterruptedException {
		FreestyleJobBean job = run.getJob();
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
