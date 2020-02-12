package restfulci.master.dao;

import java.io.IOException;

import restfulci.master.domain.GitBranchRunBean;
import restfulci.master.domain.GitCommitRunBean;
import restfulci.master.domain.RunConfigBean;

public interface RunConfigRepository {

	public RunConfigBean getConfig(GitBranchRunBean branchRun) throws IOException, InterruptedException;
	public RunConfigBean getConfig(GitCommitRunBean commitRun) throws IOException, InterruptedException;
}
