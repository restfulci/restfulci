package restfulci.slave.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;

public class DockerConfigTest {

	@Test
	public void testDockerDaemonConfig() {
		
		DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
//				.withDockerHost("unix:///var/run/docker.sock")
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
		
		Info info = dockerClient.infoCmd().exec();
		assertEquals(info.getOsType(), "linux");
	}
}
