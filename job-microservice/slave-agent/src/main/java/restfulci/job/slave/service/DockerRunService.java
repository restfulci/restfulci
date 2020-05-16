package restfulci.job.slave.service;

import java.io.IOException;

import restfulci.job.shared.domain.RunMessageBean;

public interface DockerRunService {

	public void runByMessage(RunMessageBean runMessage) throws InterruptedException, IOException;
}
