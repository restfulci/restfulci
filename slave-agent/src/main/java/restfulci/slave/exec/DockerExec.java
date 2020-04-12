package restfulci.slave.exec;

import java.nio.file.Path;
import java.util.List;

import restfulci.shared.domain.RunBean;
import restfulci.shared.domain.RunConfigBean;

public interface DockerExec {

	public void pullImage(String imageTag) throws InterruptedException;
	public String buildImageAndGetId(Path localRepoPath, RunConfigBean runConfig);
	public void runCommand(RunBean run, String imageTag, List<String> command) throws InterruptedException;
}
