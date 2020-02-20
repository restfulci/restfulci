package restfulci.master.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Date;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import restfulci.master.domain.GitBranchRunBean;
import restfulci.master.domain.GitCommitRunBean;
import restfulci.master.domain.GitJobBean;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("local")
public class RunConfigRepositoryTest {
	
	@Autowired RunConfigRepository repository;
	
	/*
	 * CircleCI doesn't support git local clone.
	 */
	@Disabled
	public void testGetJobConfigFromBranchRun(@TempDir File tmpFolder) throws Exception {
		
		File sourceDirectory = new File(tmpFolder, "source-repo");
		sourceDirectory.mkdir();
		
		Files.copy(
				new File(getClass().getClassLoader().getResource("example-restfulci.yml").getFile()).toPath(), 
				sourceDirectory.toPath().resolve(".restfulci.yml"));
		
		ProcessBuilder builder = new ProcessBuilder("git", "init");
		builder.directory(sourceDirectory);
		builder.start().waitFor();
		
		builder = new ProcessBuilder("git", "add", "-A");
		builder.directory(sourceDirectory);
		builder.start().waitFor();
		
		builder = new ProcessBuilder("git", "commit", "-m", "\"add .restfulci.yml\"");
		builder.directory(sourceDirectory);
		builder.start().waitFor();
		
		GitJobBean job = new GitJobBean();
		job.setId(123);
		job.setName("job");
		job.setRemoteOrigin("file://"+sourceDirectory.getAbsolutePath());
		job.setConfigFilepath(".restfulci.yml");
		
		GitBranchRunBean run = new GitBranchRunBean();
		run.setId(456);
		run.setJob(job);
		run.setTriggerAt(new Date(0L));
		run.setCompleteAt(new Date(1000L));
		run.setBranchName("master");
		
		assertEquals(repository.getConfig(run).getVersion(), "1.0");
	}
	
	/*
	 * CircleCI doesn't support git local clone.
	 */
	@Disabled
	public void testGetJobConfigFromCommitRun(@TempDir File tmpFolder) throws Exception {
		
		File sourceDirectory = new File(tmpFolder, "source-repo");
		sourceDirectory.mkdir();
		
		Files.copy(
				new File(getClass().getClassLoader().getResource("example-restfulci.yml").getFile()).toPath(), 
				sourceDirectory.toPath().resolve(".restfulci.yml"));
		
		ProcessBuilder builder = new ProcessBuilder("git", "init");
		builder.directory(sourceDirectory);
		builder.start().waitFor();
		
		builder = new ProcessBuilder("git", "add", "-A");
		builder.directory(sourceDirectory);
		builder.start().waitFor();
		
		builder = new ProcessBuilder("git", "commit", "-m", "\"add .restfulci.yml\"");
		builder.directory(sourceDirectory);
		builder.start().waitFor();
		
		builder = new ProcessBuilder("git", "rev-parse", "HEAD");
		builder.directory(sourceDirectory);
		Process process = builder.start();
		String commitSha = new BufferedReader(new InputStreamReader(process.getInputStream())).readLine();
		process.waitFor();
		
		GitJobBean job = new GitJobBean();
		job.setId(123);
		job.setName("job");
		job.setRemoteOrigin("file://"+sourceDirectory.getAbsolutePath());
		job.setConfigFilepath(".restfulci.yml");
		
		GitCommitRunBean run = new GitCommitRunBean();
		run.setId(456);
		run.setJob(job);
		run.setTriggerAt(new Date(0L));
		run.setCompleteAt(new Date(1000L));
		run.setCommitSha(commitSha);
		
		assertEquals(repository.getConfig(run).getVersion(), "1.0");
	}
	
	@Test
	public void testAutowired() {
		
	}
}