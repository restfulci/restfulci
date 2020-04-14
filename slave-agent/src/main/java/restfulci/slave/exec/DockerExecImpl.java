package restfulci.slave.exec;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.core.command.WaitContainerResultCallback;

import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;
import restfulci.shared.dao.MinioRepository;
import restfulci.shared.dao.RunRepository;
import restfulci.shared.domain.RunBean;
import restfulci.shared.domain.RunConfigBean;
import restfulci.shared.domain.RunPhase;

@Slf4j
@Component
public class DockerExecImpl implements DockerExec {
	
	@Autowired private DockerClient dockerClient;
	
	@Autowired private RunRepository runRepository;
	@Autowired private MinioRepository minioRepository;

	@Override
	public void pullImage(String imageTag) throws InterruptedException {

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
	public String buildImageAndGetId(Path localRepoPath, RunConfigBean runConfig) {
		
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
		
		return imageId;
	}

	@Override
	public void runCommand(RunBean run, String imageTag, List<String> command, Map<String, File> mounts) throws InterruptedException {
		
		log.info("Execute command "+command+" in docker image: "+imageTag);
		
		/*
		 * https://github.com/docker-java/docker-java/blob/3.1.5/src/test/java/com/github/dockerjava/cmd/StartContainerCmdIT.java#L50
		 */
		List<Bind> binds = new ArrayList<Bind>();
		for (Map.Entry<String, File> entry : mounts.entrySet()) {
			binds.add(new Bind(entry.getValue().getAbsolutePath(), new Volume(entry.getKey())));
		}
		
		/*
		 * https://github.com/docker-java/docker-java/blob/3.1.5/src/test/java/com/github/dockerjava/cmd/LogContainerCmdIT.java
		 */
		CreateContainerResponse container = dockerClient.createContainerCmd(imageTag)
				.withCmd(command)
				.withBinds(binds)
				.exec();
		
		int timestamp = (int) (System.currentTimeMillis() / 1000);
		dockerClient.startContainerCmd(container.getId()).exec();
		
		int exitCode = dockerClient.waitContainerCmd(container.getId())
				.exec(new WaitContainerResultCallback())
				.awaitStatusCode();
		run.setExitCode(exitCode);
		
		LogContainerCallbackWrapper loggingCallback = new LogContainerCallbackWrapper();
		dockerClient.logContainerCmd(container.getId())
				.withStdErr(true)
				.withStdOut(true)
				.withSince(timestamp)
				.exec(loggingCallback);
		loggingCallback.awaitCompletion();
		try {
			/*
			 * TODO:
			 * Directly consume InputStream coming from docker execution.
			 */
			InputStream contentStream = new ByteArrayInputStream(
					loggingCallback.toString().getBytes(StandardCharsets.UTF_8));
			minioRepository.putRunOutputAndUpdateRunBean(run, contentStream);
		} catch (MinioException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		run.setPhase(RunPhase.COMPLETE);
		run.setCompleteAt(new Date());
		runRepository.saveAndFlush(run);
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
