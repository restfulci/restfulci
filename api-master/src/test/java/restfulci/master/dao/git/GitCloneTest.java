package restfulci.master.dao.git;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class GitCloneTest {

	/*
	 * CircleCI doesn't support git local clone.
	 */
	@Disabled
	public void testGitCloneFromLocal(@TempDir File tmpFolder) throws Exception {
		
		File sourceDirectory = new File(tmpFolder, "source-repo");
		sourceDirectory.mkdir();
		
		ProcessBuilder builder = new ProcessBuilder("git", "init");
		builder.directory(sourceDirectory);
		builder.start().waitFor();
		
		builder = new ProcessBuilder("touch", "a-file.txt");
		builder.directory(sourceDirectory);
		builder.start().waitFor();
		
		builder = new ProcessBuilder("git", "add", "-A");
		builder.directory(sourceDirectory);
		builder.start().waitFor();
		
		builder = new ProcessBuilder("git", "commit", "-m", "\"add a-file.txt\"");
		builder.directory(sourceDirectory);
		builder.start().waitFor();
		
		builder = new ProcessBuilder("touch", "another-file.txt");
		builder.directory(sourceDirectory);
		builder.start().waitFor();
		
		builder = new ProcessBuilder("git", "add", "-A");
		builder.directory(sourceDirectory);
		builder.start().waitFor();
		
		builder = new ProcessBuilder("git", "commit", "-m", "\"add another-file.txt\"");
		builder.directory(sourceDirectory);
		builder.start().waitFor();
		
		File localDirectory = new File(tmpFolder, "local-repo");
		localDirectory.mkdir();
		
		GitClone gitClone = new GitClone(sourceDirectory.getAbsolutePath(), localDirectory);
		gitClone.setBranchName("master");
		gitClone.setDepth(1);
		CommandResult gitResult = gitClone.execute();
		assertTrue(gitResult.getCommand().startsWith("git clone"));
		assertTrue(gitResult.getNormalOutput().isEmpty());
		assertTrue(gitResult.getErrorOutput().startsWith("Cloning into"));
		assertEquals(gitResult.getExitCode(), 0);
		
		List<String> lsArray = new ArrayList<String>();
		lsArray.add("/bin/sh");
		lsArray.add("-c");
		lsArray.add("ls");
		CommandResult lsResult = Executable.executeWith(lsArray, localDirectory);
		assertEquals(lsResult.getNormalOutput(), "a-file.txt\nanother-file.txt\n");
	}
	
	@Test
	public void testGitCloneFromGitHubHttpsProtocol(@TempDir File tmpFolder) throws Exception {
		
		File localDirectory = new File(tmpFolder, "local-repo");
		localDirectory.mkdir();
		
		GitClone gitClone = new GitClone("https://github.com/restfulci/restfulci.git", localDirectory);
		gitClone.setBranchName("master");
		gitClone.setDepth(1);
		CommandResult gitResult = gitClone.execute();
		assertTrue(gitResult.getCommand().startsWith("git clone"));
		assertTrue(gitResult.getNormalOutput().isEmpty());
		assertTrue(gitResult.getErrorOutput().startsWith("Cloning into"));
		assertEquals(gitResult.getExitCode(), 0);
		
		List<String> lsArray = new ArrayList<String>();
		lsArray.add("/bin/sh");
		lsArray.add("-c");
		lsArray.add("ls");
		CommandResult lsResult = Executable.executeWith(lsArray, localDirectory);
		assertTrue(lsResult.getNormalOutput().contains("README.md"));
	}
	
	@Test
	public void testGitCloneFromGitHubSshProtocol(@TempDir File tmpFolder) throws Exception {
		
		File localDirectory = new File(tmpFolder, "local-repo");
		localDirectory.mkdir();
		
		GitClone gitClone = new GitClone("git@github.com:restfulci/restfulci.git", localDirectory);
		gitClone.setBranchName("master");
		gitClone.setDepth(1);
		CommandResult gitResult = gitClone.execute();
		assertTrue(gitResult.getCommand().startsWith("git clone"));
		assertTrue(gitResult.getNormalOutput().isEmpty());
		assertTrue(gitResult.getErrorOutput().startsWith("Cloning into"));
		assertEquals(gitResult.getExitCode(), 0);
		
		List<String> lsArray = new ArrayList<String>();
		lsArray.add("/bin/sh");
		lsArray.add("-c");
		lsArray.add("ls");
		CommandResult lsResult = Executable.executeWith(lsArray, localDirectory);
		assertTrue(lsResult.getNormalOutput().contains("README.md"));
	}
}
