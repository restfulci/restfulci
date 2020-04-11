package restfulci.slave.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.core.command.WaitContainerResultCallback;

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
import restfulci.slave.dto.DockerRunCmdResultDTO;

@Slf4j
@Service
public class DockerRunServiceImpl implements DockerRunService {
	
	@Autowired private DockerClient dockerClient;
	
	@Autowired private RunRepository runRepository;
	@Autowired private RemoteGitRepository remoteGitRepository;
	@Autowired private MinioRepository minioRepository;

	@Override
	public void executeRun(RunMessageBean runMessage) throws InterruptedException, IOException {
		
		RunBean run = runRepository.findById(runMessage.getRunId()).get();
		
		DockerRunCmdResultDTO result = null;
		if (run instanceof FreestyleRunBean) {
			FreestyleRunBean freestyleRun = (FreestyleRunBean)run;
			result = runFreestyleJob(freestyleRun);
			
		}
		else if (run instanceof GitRunBean) {
			GitRunBean gitRun = (GitRunBean)run;
			result = runGitJob(gitRun);
		}
		else {
			throw new IOException("Input run with wrong type");
		}
		
		System.out.println(result);
		
		run.setPhase(RunPhase.COMPLETE);
		run.setCompleteAt(new Date());
		run.setExitCode(result.getExitCode());
		
		try {
			/*
			 * TODO:
			 * Directly consume InputStream coming from docker execution.
			 */
			InputStream contentStream = new ByteArrayInputStream(
					result.getOutput().getBytes(StandardCharsets.UTF_8));
			minioRepository.putRunOutputAndUpdateRunBean(run, contentStream);
		} catch (MinioException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		runRepository.saveAndFlush(run);
	}
	
	@Override
	public DockerRunCmdResultDTO runFreestyleJob(FreestyleRunBean run) throws InterruptedException {
		FreestyleJobBean job = run.getJob();
		pullImage(job.getDockerImage());
		return runCommand(job.getDockerImage(), Arrays.asList(job.getCommand()));
	}
	
	@Override
	public DockerRunCmdResultDTO runGitJob(GitRunBean run) throws InterruptedException, IOException {
		
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

		/*
		 * https://github.com/docker-java/docker-java/blob/3.1.5/src/test/java/com/github/dockerjava/cmd/BuildImageCmdIT.java
		 */
		String imageId = dockerClient
				.buildImageCmd()
				.withBaseDirectory(runConfig.getBaseDir(localRepoPath))
				.withDockerfile(runConfig.getDockerfile(localRepoPath))
				.withNoCache(true)
				.exec(new BuildImageResultCallback())
				.awaitImageId();
		
		return runCommand(imageId, runConfig.getCommand());
	}
	
	private void pullImage(String imageTag) throws InterruptedException {
		
		/*
		 * TODO:
		 * Not exactly sure the behavior if the tag is `:latest`,
		 * but for fixed version pretty sure it works.
		 */
		List<Image> localImages = dockerClient.listImagesCmd().withShowAll(true).exec();
		for (Image image : localImages) {
			for (int i = 0; i < image.getRepoTags().length; ++i) {
				if (image.getRepoTags()[i].equals(imageTag)) {
					log.info("Docker image already exists in local: "+imageTag);
					return;
				}
			}
		}
		
		log.info("Pulling new docker image from remote server: "+imageTag);
		dockerClient.pullImageCmd(imageTag)
				.exec(new PullImageResultCallback())
				.awaitCompletion(30, TimeUnit.SECONDS);	
	}

	@Override
	public DockerRunCmdResultDTO runCommand(String imageTag, List<String> command) throws InterruptedException {
		
		log.info("Execute command "+command+" in docker image: "+imageTag);
		
		CreateContainerResponse container = dockerClient.createContainerCmd(imageTag)
				.withCmd(command)
				.exec();
		
		int timestamp = (int) (System.currentTimeMillis() / 1000);
		dockerClient.startContainerCmd(container.getId()).exec();
		
		DockerRunCmdResultDTO result = new DockerRunCmdResultDTO();

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
