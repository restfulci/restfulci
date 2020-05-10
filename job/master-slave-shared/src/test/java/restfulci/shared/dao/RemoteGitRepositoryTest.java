package restfulci.shared.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import restfulci.shared.domain.GitBranchRunBean;
import restfulci.shared.domain.GitCommitRunBean;
import restfulci.shared.domain.GitJobBean;
import restfulci.shared.domain.RunConfigBean;

/*
 * CircleCI doesn't support git local clone.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@DisabledIfEnvironmentVariable(named="CI", matches="CircleCI")
public class RemoteGitRepositoryTest {
	
	@Autowired private RemoteGitRepository repository;
	
	@Test
	public void testCopyToLocalAndGetConfigFromBranchRun(@TempDir File tmpFolder) throws Exception {
		
		File sourceDirectory = new File(tmpFolder, "source-repo");
		sourceDirectory.mkdir();
		setupRemoteGit(sourceDirectory);
		
		GitBranchRunBean run = setupBranchRun(sourceDirectory);
		
		File targetDirectory = new File(tmpFolder, "local-repo");
		targetDirectory.mkdir();
		Path targetPath = targetDirectory.toPath();
		repository.copyToLocal(run, targetPath);
		
		List<Object> localPathnames = Arrays.asList(targetDirectory.list());
		
		assertEquals(localPathnames.size(), 2);
		assertTrue(localPathnames.contains("restfulci.yml"));
		assertTrue(localPathnames.contains(".git"));
		
		RunConfigBean runConfig = repository.getConfigFromFilepath(run, targetPath);
		assertEquals(runConfig.getVersion(), "1.0");
	}
	
	@Test
	public void testCopyToLocalAndGetConfigFromCommitRun(@TempDir File tmpFolder) throws Exception {
		
		File sourceDirectory = new File(tmpFolder, "source-repo");
		sourceDirectory.mkdir();
		String commitSha = setupRemoteGit(sourceDirectory);
		
		GitCommitRunBean run = setupCommitRun(sourceDirectory, commitSha);
		
		File targetDirectory = new File(tmpFolder, "local-repo");
		targetDirectory.mkdir();
		Path targetPath = targetDirectory.toPath();
		repository.copyToLocal(run, targetPath);
		
		List<Object> localPathnames = Arrays.asList(targetDirectory.list());
		
		assertEquals(localPathnames.size(), 2);
		assertTrue(localPathnames.contains("restfulci.yml"));
		assertTrue(localPathnames.contains(".git"));
		
		RunConfigBean runConfig = repository.getConfigFromFilepath(run, targetPath);
		assertEquals(runConfig.getVersion(), "1.0");
	}
	
	private String setupRemoteGit(File sourceDirectory) throws InterruptedException, IOException {
		
		Files.copy(
				new File(getClass().getClassLoader().getResource("restfulci-from-build.yml").getFile()).toPath(), 
				sourceDirectory.toPath().resolve("restfulci.yml"));
		
		ProcessBuilder builder = new ProcessBuilder("git", "init");
		builder.directory(sourceDirectory);
		builder.start().waitFor();
		
		builder = new ProcessBuilder("git", "add", "-A");
		builder.directory(sourceDirectory);
		builder.start().waitFor();
		
		builder = new ProcessBuilder("git", "commit", "-m", "\"add restfulci.yml\"");
		builder.directory(sourceDirectory);
		builder.start().waitFor();
		
		builder = new ProcessBuilder("git", "rev-parse", "HEAD");
		builder.directory(sourceDirectory);
		Process process = builder.start();
		String commitSha = new BufferedReader(new InputStreamReader(process.getInputStream())).readLine();
		process.waitFor();
		
		return commitSha;
	}
	
	private GitBranchRunBean setupBranchRun(File sourceDirectory) {
		
		GitJobBean job = new GitJobBean();
		job.setId(123);
		job.setName("job");
		job.setRemoteOrigin("file://"+sourceDirectory.getAbsolutePath());
		job.setConfigFilepath("restfulci.yml");
		
		GitBranchRunBean run = new GitBranchRunBean();
		run.setId(456);
		run.setJob(job);
		run.setTriggerAt(new Date(0L));
		run.setCompleteAt(new Date(1000L));
		run.setBranchName("master");
		
		return run;
	}
	
	private GitCommitRunBean setupCommitRun(File sourceDirectory, String commitSha) {
		
		GitJobBean job = new GitJobBean();
		job.setId(123);
		job.setName("job");
		job.setRemoteOrigin("file://"+sourceDirectory.getAbsolutePath());
		job.setConfigFilepath("restfulci.yml");
		
		GitCommitRunBean run = new GitCommitRunBean();
		run.setId(456);
		run.setJob(job);
		run.setTriggerAt(new Date(0L));
		run.setCompleteAt(new Date(1000L));
		run.setCommitSha(commitSha);
		
		return run;
	}
}