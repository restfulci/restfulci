package restfulci.slave.service;

import java.io.IOException;

import restfulci.shared.domain.DockerRunCmdResultBean;
import restfulci.shared.domain.RunMessageBean;

public interface DockerRunService {

	public void executeRun(RunMessageBean runMessage) throws InterruptedException, IOException;
	
	public DockerRunCmdResultBean runCommand(String image, String[] command) throws InterruptedException;
}
