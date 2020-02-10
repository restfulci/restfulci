package restfulci.master.dao;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.stereotype.Repository;

import restfulci.master.dao.git.GitClone;
import restfulci.master.domain.GitBranchRunBean;

@Repository
public class RunConfigRepositoryImpl implements RunConfigRepository {

	@Override
	public String getConfig(GitBranchRunBean branchRun) throws IOException, InterruptedException {
		
		Path localRepoPath = Files.createTempDirectory("local-repo");
		
		GitClone gitClone = new GitClone(branchRun.getJob().getRemoteOrigin(), localRepoPath.toFile());
		gitClone.execute();
		
		List<String> lines = Files.readAllLines(localRepoPath.resolve(branchRun.getJob().getConfigFilepath()));
		return String.join("\n", lines);
	}

}
