package restfulci.shared.dao;

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
import restfulci.shared.domain.RunConfigBean;

@Repository
public class RunConfigRepositoryImpl implements RunConfigRepository {

	@Override
	public RunConfigBean getConfig(GitBranchRunBean branchRun) throws IOException, InterruptedException {
		
		Path localRepoPath = Files.createTempDirectory("local-repo");
		
		GitClone gitClone = new GitClone(branchRun.getJob().getRemoteOrigin(), localRepoPath.toFile());
		gitClone.setBranchName(branchRun.getBranchName());
		gitClone.setDepth(1);
		gitClone.execute();
		
		return getConfigFromFilepath(localRepoPath, branchRun.getJob().getConfigFilepath());
	}

	/*
	 * TODO:
	 * Not supported by GitHub. Error message:
	 * ```
	 * git fetch --depth=1 git@github.com:restfulci/restfulci.git 1d0e1224b401490610b8cc257bedff35b0689cb5
	 * error: Server does not allow request for unadvertised object 1d0e1224b401490610b8cc257bedff35b0689cb5
	 * ```
	 */
	@Override
	public RunConfigBean getConfig(GitCommitRunBean commitRun) throws IOException, InterruptedException {
		
		Path localRepoPath = Files.createTempDirectory("local-repo");
		
		GitInit gitInit = new GitInit(localRepoPath.toFile());
		gitInit.execute();
		
		GitFetch gitFetch = new GitFetch(commitRun.getJob().getRemoteOrigin(), localRepoPath.toFile());
		gitFetch.setCommitSha(commitRun.getCommitSha());
		gitFetch.setDepth(1);
		gitFetch.execute();
		
		GitCheckout gitCheckout = new GitCheckout(localRepoPath.toFile());
		gitCheckout.setCommitSha(commitRun.getCommitSha());
		gitCheckout.setForce(true);
		gitCheckout.execute();
		
		return getConfigFromFilepath(localRepoPath, commitRun.getJob().getConfigFilepath());
	}
	
	private RunConfigBean getConfigFromFilepath(Path localRepoPath, String configFilepath) throws IOException {
		String yamlContent = String.join("\n", Files.readAllLines(localRepoPath.resolve(configFilepath)));
		return RunConfigYamlParser.parse(yamlContent);
	}
}
