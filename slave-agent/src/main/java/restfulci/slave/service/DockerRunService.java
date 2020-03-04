package restfulci.slave.service;

import java.util.ArrayList;
import java.util.List;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.WaitContainerResultCallback;

import restfulci.shared.domain.DockerRunCmdResultBean;

////import com.github.dockerjava.utils.LogContainerTestCallback;
//import com.github.dockerjava.api.async.ResultCallback;
//import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.command.LogContainerResultCallback;

public class DockerRunService {
	
	/*
	 * Copyright (c) docker-java
	 * https://github.com/docker-java/docker-java/blob/3.1.5/src/test/java/com/github/dockerjava/utils/LogContainerTestCallback.java#L3
	 */
	public class LogContainerCallbackWrapper extends LogContainerResultCallback {
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


		public List<Frame> getCollectedFrames() {
			return collectedFrames;
		}
	}

	public DockerRunCmdResultBean runCommand(String image, List<String> command) throws InterruptedException {
		
		DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
				.withDockerHost("unix:///var/run/docker.sock")
				.build();
		DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();
		
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
}
