package restfulci.slave.config.bean;

import lombok.Getter;

@Getter
public class DockerDaemon {

	final private String dockerHost;
	
	public DockerDaemon(String dockerHost) {
		this.dockerHost = dockerHost;
	}
}
