package restfulci.shared.dao.git;

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
public class GitCheckout extends Executable {
	
	public GitCheckout (File directory) {
		this.directory = directory;
	}
	
	private File directory;
	private String commitSha;
	private boolean force = false;

	public CommandResult execute() throws IOException, InterruptedException {
		
		List<String> commandArray = new ArrayList<String>();
		commandArray.add("git");
		commandArray.add("checkout");
		
		if (force == true) {
			commandArray.add("-f");
		}
		
		commandArray.add(commitSha);
		
		return executeWith(commandArray, directory);
	}
}
