package restfulci.job.slave.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;

@Configuration
public class DockerConfig {

	@Bean
	@Profile("dev")
	public DockerClient devDockerClient() {
		
		DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
				.withDockerHost("unix:///var/run/docker.sock")
				.build();
		DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();
		return dockerClient;
	}
	
	@Bean
	@Profile("docker")
	public DockerClient dockerDockerClient() {
		
		DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
				.withDockerHost("unix:///var/run/docker.sock")
				.build();
		DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();
		return dockerClient;
	}
	
	@Bean
	@Profile("kubernetes")
	public DockerClient kubernetesDockerClient() {
		
		DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
				.withDockerHost("unix:///var/run/docker.sock")
				.build();
		DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();
		return dockerClient;
	}
	
	@Bean
	@Profile("circleci")
	public DockerClient circleciDockerClient() {
		
		DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
				/*
				 * Need to disable this specified UNIX socket, otherwise CircleCI
				 * test will fail.
				 */
//				.withDockerHost("unix:///var/run/docker.sock")
				.build();
		DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();
		return dockerClient;
	}
}
