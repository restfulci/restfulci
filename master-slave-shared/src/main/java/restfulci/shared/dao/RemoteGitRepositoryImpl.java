package restfulci.shared.dao;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Repository;

import restfulci.shared.dao.git.GitCheckout;
import restfulci.shared.dao.git.GitClone;
import restfulci.shared.dao.git.GitFetch;
import restfulci.shared.dao.git.GitInit;
import restfulci.shared.domain.GitBranchRunBean;
import restfulci.shared.domain.GitCommitRunBean;
import restfulci.shared.domain.GitRunBean;
import restfulci.shared.domain.RunConfigBean;

@Repository
public class RemoteGitRepositoryImpl implements RemoteGitRepository {
	
	@Override
	public void copyToLocal(GitRunBean run, Path localRepoPath) throws IOException, InterruptedException {
		
		if (run instanceof GitBranchRunBean) {
			copyBranchToLocal((GitBranchRunBean)run, localRepoPath);
		}
		else if (run instanceof GitCommitRunBean) {
			copyCommitToLocal((GitCommitRunBean)run, localRepoPath);
		}
	}

	private void copyBranchToLocal(GitBranchRunBean branchRun, Path localRepoPath) throws IOException, InterruptedException {
		
		File targetFolder = localRepoPath.toFile();
		
		GitClone gitClone = new GitClone(branchRun.getJob().getRemoteOrigin(), targetFolder);
		gitClone.setBranchName(branchRun.getBranchName());
		gitClone.setDepth(1);
		gitClone.execute();
	}

	/*
	 * TODO:
	 * Not supported by GitHub. Error message:
	 * ```
	 * git fetch --depth=1 git@github.com:restfulci/restfulci.git 1d0e1224b401490610b8cc257bedff35b0689cb5
	 * error: Server does not allow request for unadvertised object 1d0e1224b401490610b8cc257bedff35b0689cb5
	 * ```
	 */
	private void copyCommitToLocal(GitCommitRunBean commitRun, Path localRepoPath) throws IOException, InterruptedException {
		
		File targetFolder = localRepoPath.toFile();
		
		GitInit gitInit = new GitInit(targetFolder);
		gitInit.execute();
		
		GitFetch gitFetch = new GitFetch(commitRun.getJob().getRemoteOrigin(), targetFolder);
		gitFetch.setCommitSha(commitRun.getCommitSha());
		gitFetch.setDepth(1);
		gitFetch.execute();
		
		GitCheckout gitCheckout = new GitCheckout(targetFolder);
		gitCheckout.setCommitSha(commitRun.getCommitSha());
		gitCheckout.setForce(true);
		gitCheckout.execute();
	}
	
	@Override
	public RunConfigBean getConfigFromFilepath(GitRunBean run, Path localRepoPath) throws IOException {
		
		String configFilepath = run.getJob().getConfigFilepath();
		String yamlContent = String.join("\n", Files.readAllLines(localRepoPath.resolve(configFilepath)));
		return RunConfigYamlParser.parse(yamlContent);
	}
}
