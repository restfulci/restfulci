package restfulci.shared.dao;

import java.io.File;
import java.io.IOException;

import restfulci.shared.domain.GitBranchRunBean;
import restfulci.shared.domain.GitCommitRunBean;
import restfulci.shared.domain.RunConfigBean;

public interface RemoteGitRepository {

	public void copyToLocal(GitBranchRunBean branchRun, File targetFolder) throws IOException, InterruptedException;
	public void copyToLocal(GitCommitRunBean commitRun, File targetFolder) throws IOException, InterruptedException;
	
	public RunConfigBean getConfig(GitBranchRunBean branchRun) throws IOException, InterruptedException;
	public RunConfigBean getConfig(GitCommitRunBean commitRun) throws IOException, InterruptedException;
}
