package restfulci.shared.dao;

import java.io.IOException;

import restfulci.shared.domain.GitBranchRunBean;
import restfulci.shared.domain.GitCommitRunBean;
import restfulci.shared.domain.RunConfigBean;

public interface RunConfigRepository {

	public RunConfigBean getConfig(GitBranchRunBean branchRun) throws IOException, InterruptedException;
	public RunConfigBean getConfig(GitCommitRunBean commitRun) throws IOException, InterruptedException;
}
