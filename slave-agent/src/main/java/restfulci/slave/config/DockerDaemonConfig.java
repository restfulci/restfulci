package restfulci.slave.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import restfulci.slave.config.bean.DockerDaemon;

@Configuration
public class DockerDaemonConfig {

	@Bean
	public DockerDaemon dockerDaemon() {
		return new DockerDaemon("unix:///var/run/docker.sock");
	}
}
