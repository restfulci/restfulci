package restfulci.job.shared.dao;

import java.io.IOException;
import java.nio.file.Path;

import restfulci.job.shared.domain.GitRunBean;
import restfulci.job.shared.domain.RunConfigBean;

public interface RemoteGitRepository {

	public void copyToLocal(GitRunBean run, Path localRepoPath) throws IOException, InterruptedException;
	
	public Path getConfigFilepath(GitRunBean run, Path localRepoPath) throws IOException;
	public RunConfigBean getConfigFromFilepath(GitRunBean run, Path localRepoPath) throws IOException;
}
