package restfulci.slave.service;

import java.io.IOException;
import java.util.List;

import restfulci.shared.domain.FreestyleRunBean;
import restfulci.shared.domain.GitRunBean;
import restfulci.shared.domain.RunMessageBean;
import restfulci.slave.dto.DockerRunCmdResultDTO;

public interface DockerRunService {

	public void executeRun(RunMessageBean runMessage) throws InterruptedException, IOException;
	
	public DockerRunCmdResultDTO runFreestyleJob(FreestyleRunBean run) throws InterruptedException;
	public DockerRunCmdResultDTO runGitJob(GitRunBean run) throws InterruptedException, IOException;
	
	public DockerRunCmdResultDTO runCommand(String imageTag, List<String> command) throws InterruptedException;
}
