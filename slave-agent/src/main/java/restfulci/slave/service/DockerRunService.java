package restfulci.slave.service;

import java.io.IOException;

import restfulci.shared.domain.RunMessageBean;

public interface DockerRunService {

	public void runByMessage(RunMessageBean runMessage) throws InterruptedException, IOException;
}
