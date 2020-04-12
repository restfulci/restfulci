package restfulci.slave.service;

import java.io.IOException;
import java.util.List;

import restfulci.shared.domain.FreestyleRunBean;
import restfulci.shared.domain.GitRunBean;
import restfulci.shared.domain.RunBean;
import restfulci.shared.domain.RunMessageBean;

public interface DockerRunService {

	public void executeRun(RunMessageBean runMessage) throws InterruptedException, IOException;
	
	public void runFreestyleJob(FreestyleRunBean run) throws InterruptedException;
	public void runGitJob(GitRunBean run) throws InterruptedException, IOException;
	
	public void runCommand(RunBean run, String imageTag, List<String> command) throws InterruptedException;
}
