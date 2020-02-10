package restfulci.master.dao;

import java.io.IOException;

import restfulci.master.domain.GitBranchRunBean;

public interface RunConfigRepository {

	public String getConfig(GitBranchRunBean branchRun) throws IOException, InterruptedException;
}
