package restfulci.slave.service;

import java.util.List;

import restfulci.shared.domain.DockerRunCmdResultBean;

public interface DockerRunService {

	public DockerRunCmdResultBean runCommand(String image, List<String> command) throws InterruptedException;
}
