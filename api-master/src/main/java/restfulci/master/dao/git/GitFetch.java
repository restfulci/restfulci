package restfulci.master.dao.git;

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
public class GitFetch extends Executable {
	
	public GitFetch (String URI, File directory) {
		this.URI = URI;
		this.directory = directory;
	}
	
	private String URI;
	private File directory;
	private String commitSha;
	private Integer depth;

	public CommandResult execute() throws IOException, InterruptedException {
		
		List<String> commandArray = new ArrayList<String>();
		commandArray.add("git");
		commandArray.add("fetch");
		
		if (depth != null) {
			commandArray.add("--depth");
			commandArray.add(depth.toString());
		}
		
		commandArray.add(URI);
		
		if (commitSha != null) {
			commandArray.add(commitSha);
		}
		
		return executeWith(commandArray, directory);
	}
}
