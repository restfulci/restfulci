package restfulci.job.shared.dao.git;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.Setter;

/*
 * Cannot use JGit, because it supports either (1) shallow clone, or
 * (2) single file clone.
 */
@Setter
public class GitClone extends Executable {
	
	public GitClone (String URI, File directory) {
		this.URI = URI;
		this.directory = directory;
	}
	
	private String URI;
	private File directory;
	private String branchName;
	private Integer depth;

	public CommandResult execute() throws IOException, InterruptedException {
		
		List<String> commandArray = new ArrayList<String>();
		commandArray.add("git");
		commandArray.add("clone");
		commandArray.add(URI);
		
		if (branchName != null) {
			commandArray.add("-b");
			commandArray.add(branchName);
			commandArray.add("--single-branch");
		}
		
		if (depth != null) {
			commandArray.add("--depth");
			commandArray.add(depth.toString());
		}
		
		commandArray.add(directory.getAbsolutePath());
		
		CommandResult commandResult = executeWith(commandArray, directory);
		
		if (commandResult.getExitCode() == 128) {
			if (commandResult.getErrorOutput().contains("Repository not found")) {
				throw new GitRepoNonExistException(commandResult.getErrorOutput());
			}
			if (commandResult.getErrorOutput().contains("Could not find remote branch")) {
				throw new GitBranchNonExistException(commandResult.getErrorOutput());
			}
		}
		
		return commandResult;
	}
}
