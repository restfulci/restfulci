package restfulci.slave.config;

import org.junit.jupiter.api.Test;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;

public class DockerDaemonConfigTest {

	@Test
	public void testDockerDaemonConfig() {
		
		DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
				.withDockerHost("tcp://localhost:2375")
//				.withDockerTlsVerify(true)
//				.withDockerCertPath("/home/user/.docker/certs")
//				.withDockerConfig("/home/user/.docker")
//				.withApiVersion("1.30") // optional
//				.withRegistryUrl("https://index.docker.io/v1/")
//				.withRegistryUsername("dockeruser")
//				.withRegistryPassword("ilovedocker")
//				.withRegistryEmail("dockeruser@github.com")
			    .build();
		DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();
		
//		Info info = dockerClient.infoCmd().exec();
//		System.out.print(info);
	}
}
