package restfulci.job.slave.exec;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import com.github.dockerjava.api.model.Network;

import restfulci.job.shared.domain.RunBean;
import restfulci.job.shared.domain.RunConfigBean;

public interface DockerExec {
	
	public Network createNetworkIfNotExist(String networkName); 

	public void pullImage(String imageTag) throws InterruptedException;
	public String buildImageAndGetId(Path localRepoPath, RunConfigBean runConfig);
	public void runCommandAndUpdateRunBean(
			RunBean run, 
			String imageTag, 
			String networkName,
			List<String> command, 
			Map<String, String> inputs,
			Map<RunConfigBean.RunConfigResultBean, File> mounts) throws InterruptedException;
}
