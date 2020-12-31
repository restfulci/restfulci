package restfulci.job.slave.exec;

import static com.github.dockerjava.api.model.HostConfig.newHostConfig;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.CreateNetworkResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.api.model.Volume;

import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;
import restfulci.job.shared.dao.MinioRepository;
import restfulci.job.shared.domain.RunBean;
import restfulci.job.shared.domain.RunConfigBean;

@Slf4j
@Component
public class DockerExecImpl implements DockerExec {
	
	@Autowired private DockerClient dockerClient;
	
	@Autowired private MinioRepository minioRepository;
	
	@Override
	public Network createNetworkIfNotExist(String networkName) {
		/*
		 * https://github.com/docker-java/docker-java/blob/3.2.7/docker-java/src/test/java/com/github/dockerjava/cmd/InspectNetworkCmdIT.java
		 * 
		 * Looks like without it docker-java will just create networks with the same name:
		 * > $ docker network ls
		 * > 1718f7c3e5d3        restfulci-unit-test             bridge              local
		 * > eb87afb4dfe0        restfulci-unit-test             bridge              local
		 * > 7efc6e8e5368        restfulci-unit-test             bridge              local
		 */
		List<Network> networks = dockerClient.listNetworksCmd().exec();
		for (Network network : networks) {
			if (network.getName().equals(networkName)) {
				return network;
			}
		}
		
		/*
		 * https://github.com/docker-java/docker-java/blob/3.2.7/docker-java/src/test/java/com/github/dockerjava/cmd/CreateNetworkCmdIT.java
		 */
		CreateNetworkResponse createNetworkResponse = dockerClient.createNetworkCmd().withName(networkName).exec();
		Network network = dockerClient.inspectNetworkCmd().withNetworkId(createNetworkResponse.getId()).exec();
		return network;
	}

	@Override
	public void pullImage(String imageTag) throws InterruptedException {

		if (!imageTag.contains(":")) {
			imageTag += ":latest";
		}
		
		List<Image> localImages = dockerClient.listImagesCmd().withShowAll(true).exec();
		for (Image image : localImages) {
			if (image.getRepoTags() != null) {
				for (int i = 0; i < image.getRepoTags().length; ++i) {
					if (image.getRepoTags()[i].equals(imageTag)) {
						log.info("Docker image already exists in local: {}", imageTag);
						return;
					}
				}
			}
		}
		
		log.info("Pulling new docker image from remote server: {}", imageTag);
		dockerClient.pullImageCmd(imageTag)
				.start()
				.awaitCompletion(30, TimeUnit.SECONDS);
	}
	
	@Override
	public String buildImageAndGetId(Path localRepoPath, RunConfigBean runConfig) {
		
		/*
		 * https://github.com/docker-java/docker-java/blob/3.2.7/docker-java/src/test/java/com/github/dockerjava/cmd/BuildImageCmdIT.java
		 */
		log.info(
				"Build image from context path {} and Dockerfile path {}",
				runConfig.getEnvironment().getBuild().getContext(),
				runConfig.getEnvironment().getBuild().getDockerfile());
		String imageId = dockerClient
				.buildImageCmd()
				.withBaseDirectory(runConfig.getBaseDir(localRepoPath))
				.withDockerfile(runConfig.getDockerfile(localRepoPath))
				.withNoCache(true)
				.start()
				.awaitImageId();
		
		return imageId;
	}

	@Override
	public void runCommandAndUpdateRunBean(
			RunBean run, 
			String imageTag, 
			String networkName,
			List<String> command, 
			Map<String, String> inputs,
			Map<RunConfigBean.RunConfigResultBean, File> mounts) throws InterruptedException {
		
		log.info("Execute command {} in docker image: {}", command, imageTag);
		
		/*
		 * https://github.com/docker-java/docker-java/issues/933#issuecomment-336422012
		 */
		List<String> inputList = new ArrayList<String>();
		for (Map.Entry<String, String> entry : inputs.entrySet()) {
			inputList.add(entry.getKey()+"="+entry.getValue());
		}
		
		/*
		 * https://github.com/docker-java/docker-java/blob/3.1.5/src/test/java/com/github/dockerjava/cmd/StartContainerCmdIT.java#L50
		 * 
		 * TODO:
		 * Currently doesn't work in Kubernetes if we map `/tmp` with host.
		 * Otherwise will face error (all tests, not only the ones with
		 * result involved):
		 * > javax.ws.rs.ProcessingException: java.io.IOException: Couldn't load native library
		 * > ...
		 * > caused by: java.lang.NoClassDefFoundError: Could not initialize class org.newsclub.net.unix.NativeUnixSocket
		 * > at org.newsclub.net.unix.AFUNIXSocket.setIsCreated(AFUNIXSocket.java:54)
		 * 
		 * Tried to comment out `.withDockerHost("unix:///var/run/docker.sock")` but that doesn't work.
		 *
		 * However, if map /tmp and run
		 * > /restfulci-examples/python-pytest # docker build -t foo . && docker run -v /tmp/bbb:/code/test-results -it foo pytest
		 * we can successfully map the result back to slave-executor's /tmp.
		 * 
		 * Seems a docker-java bug. May check if it helps to upgrade/downgrade docker-java version.
		 * https://stackoverflow.com/questions/57339358/how-to-run-a-docker-container-from-within-java
		 */
		List<Bind> binds = new ArrayList<Bind>();
		for (Map.Entry<RunConfigBean.RunConfigResultBean, File> entry : mounts.entrySet()) {
			binds.add(new Bind(entry.getValue().getAbsolutePath(), new Volume(entry.getKey().getPath())));
		}
			
		/*
		 * https://github.com/docker-java/docker-java/blob/3.2.7/docker-java/src/test/java/com/github/dockerjava/cmd/StartContainerCmdIT.java
		 */
		CreateContainerResponse container = dockerClient.createContainerCmd(imageTag)
				.withCmd(command)
				.withEnv(inputList)
				.withHostConfig(newHostConfig().withNetworkMode(networkName).withBinds(binds))
				.exec();
		
		int timestamp = (int) (System.currentTimeMillis() / 1000);
		dockerClient.startContainerCmd(container.getId()).exec();
		
		int exitCode = dockerClient.waitContainerCmd(container.getId())
				.start()
				.awaitStatusCode();
		run.setExitCode(exitCode);
		log.info("Execute command exit code: {}", exitCode);
		
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
			log.info("Execute command output: \n{}", loggingCallback.toString());
		} catch (MinioException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * Copyright (c) docker-java
	 * https://github.com/docker-java/docker-java/blob/3.2.7/docker-java/src/test/java/com/github/dockerjava/utils/LogContainerTestCallback.java
	 */
	private class LogContainerCallbackWrapper extends ResultCallback.Adapter<Frame> {
		protected final StringBuffer log = new StringBuffer();

		List<Frame> collectedFrames = new ArrayList<>();

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
