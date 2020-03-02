package restfulci.slave.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import restfulci.slave.config.bean.DockerDaemon;

@Configuration
public class DockerDaemonConfig {

	@Profile("local")
	@Bean
	public DockerDaemon stsDockerDaemon() {
		return new DockerDaemon("tcp://localhost:2375");
	}
	
	@Profile("docker")
	@Bean
	public DockerDaemon dockerDockerDaemon() {
		return new DockerDaemon("tcp://slave-docker:2375");
	}
}
