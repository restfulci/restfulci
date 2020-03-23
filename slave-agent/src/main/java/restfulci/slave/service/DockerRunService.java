package restfulci.slave.service;

import java.io.IOException;
import java.util.List;

import restfulci.shared.domain.DockerRunCmdResultBean;
import restfulci.shared.domain.FreestyleRunBean;
import restfulci.shared.domain.GitRunBean;
import restfulci.shared.domain.RunMessageBean;

public interface DockerRunService {

	public void executeRun(RunMessageBean runMessage) throws InterruptedException, IOException;
	
	public DockerRunCmdResultBean runFreestyleJob(FreestyleRunBean run) throws InterruptedException;
	public DockerRunCmdResultBean runGitJob(GitRunBean run) throws InterruptedException, IOException;
	
	public DockerRunCmdResultBean runCommand(String imageTag, List<String> command) throws InterruptedException;
}
