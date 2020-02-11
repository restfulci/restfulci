package restfulci.master.dao;

import java.io.IOException;

import restfulci.master.domain.GitBranchRunBean;
import restfulci.master.domain.GitCommitRunBean;

public interface RunConfigRepository {

	public String getConfig(GitBranchRunBean branchRun) throws IOException, InterruptedException;
	public String getConfig(GitCommitRunBean commitRun) throws IOException, InterruptedException;
}
