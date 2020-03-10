package restfulci.slave.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import restfulci.shared.dao.RemoteGitRepository;
import restfulci.shared.dao.RunRepository;
import restfulci.shared.domain.DockerRunCmdResultBean;
import restfulci.shared.domain.FreestyleJobBean;
import restfulci.shared.domain.FreestyleRunBean;
import restfulci.shared.domain.GitBranchRunBean;
import restfulci.shared.domain.GitJobBean;
import restfulci.shared.domain.GitRunBean;
import restfulci.shared.domain.RunBean;
import restfulci.shared.domain.RunMessageBean;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class DockerRunServiceTest {
	
	@Autowired private DockerRunService service;
	
	@MockBean private RunRepository runRepository;
	@SpyBean private RemoteGitRepository remoteGitRepository;
	
	@Test
	public void testExecuteRun() throws Exception{
		
		RunMessageBean runMessage = new RunMessageBean();
		runMessage.setJobId(123);
		runMessage.setRunId(456);
		
		FreestyleJobBean job = new FreestyleJobBean();
		job.setId(123);
		job.setName("job");
		job.setDockerImage("busybox");
		job.setCommand(new String[] {"sh", "-c", "echo \"Hello world\""});
		
		FreestyleRunBean run = new FreestyleRunBean();
		run.setId(456);
		run.setJob(job);
		run.setTriggerAt(new Date(0L));
		run.setCompleteAt(new Date(1000L));
		
		Optional<RunBean> maybeRun = Optional.of(run);
		given(runRepository.findById(456)).willReturn(maybeRun);
		
		service.executeRun(runMessage);
	}
	
	@Test
	public void testRunFreestyleJob() throws Exception {
		
		FreestyleJobBean job = new FreestyleJobBean();
		job.setId(123);
		job.setName("job");
		job.setDockerImage("busybox");
		job.setCommand(new String[] {"sh", "-c", "echo \"Hello world\""});
		
		FreestyleRunBean run = new FreestyleRunBean();
		run.setId(456);
		run.setJob(job);
		run.setTriggerAt(new Date(0L));
		run.setCompleteAt(new Date(1000L));
		
		DockerRunCmdResultBean result = service.runFreestyleJob(run);
		assertEquals(result.getOutput(), "Hello world\n");
	}
	
	@Test 
	public void testRunGitJob() throws Exception {
		
		GitJobBean job = new GitJobBean();
		job.setId(123);
		job.setName("job");
		job.setRemoteOrigin("git@github.com:dummy/dummy.git");
		job.setConfigFilepath(".restfulci.yml");
		
		GitBranchRunBean run = new GitBranchRunBean();
		run.setId(456);
		run.setJob(job);
		run.setBranchName("master");
		run.setTriggerAt(new Date(0L));
		run.setCompleteAt(new Date(1000L));
		
		doAnswer(new Answer<Void>() {
			public Void answer(InvocationOnMock invocation) {
				Path localRepoPath = (Path) invocation.getArguments()[1];
				try {
					Files.copy(
							new File(getClass().getClassLoader().getResource("docker-run-service-test/restfulci.yml").getFile()).toPath(), 
							localRepoPath.resolve(".restfulci.yml"));
					Files.copy(
							new File(getClass().getClassLoader().getResource("docker-run-service-test/Dockerfile").getFile()).toPath(), 
							localRepoPath.resolve("Dockerfile"));
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}
		 }).when(remoteGitRepository).copyToLocal(any(GitRunBean.class), any(Path.class));
		
		DockerRunCmdResultBean result = service.runGitJob(run);
		assertEquals(result.getOutput(), "Hello world\n");
	}

	@Test
	public void testRunCommand() throws Exception {
		
		String[] command = new String[] {"sh", "-c", "echo \"Hello world\""};
		DockerRunCmdResultBean result = service.runCommand("busybox", Arrays.asList(command));
		
		assertEquals(result.getExitCode(), 0);
		assertEquals(result.getOutput(), "Hello world\n");
	}
}
