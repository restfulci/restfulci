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
public class GitInit extends Executable {
	
	public GitInit (File directory) {
		this.directory = directory;
	}
	
	private File directory;

	public CommandResult execute() throws IOException, InterruptedException {
		
		List<String> commandArray = new ArrayList<String>();
		commandArray.add("git");
		commandArray.add("init");
		
		return executeWith(commandArray, directory);
	}
}
