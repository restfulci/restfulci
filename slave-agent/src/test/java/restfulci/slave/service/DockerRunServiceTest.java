package restfulci.slave.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
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
import restfulci.shared.domain.FreestyleJobBean;
import restfulci.shared.domain.FreestyleRunBean;
import restfulci.shared.domain.GitBranchRunBean;
import restfulci.shared.domain.GitJobBean;
import restfulci.shared.domain.GitRunBean;
import restfulci.shared.domain.RunBean;
import restfulci.shared.domain.RunMessageBean;
import restfulci.shared.domain.RunPhase;
import restfulci.slave.dto.DockerRunCmdResultDTO;

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
		job.setDockerImage("busybox:1.31");
		job.setCommand(new String[] {"sh", "-c", "echo \"Hello world\""});
		
		FreestyleRunBean run = new FreestyleRunBean();
		run.setId(456);
		run.setJob(job);
		run.setPhase(RunPhase.IN_PROGRESS);
		run.setTriggerAt(new Date(0L));
		run.setCompleteAt(new Date(1000L));
		
		Optional<RunBean> maybeRun = Optional.of(run);
		given(runRepository.findById(456)).willReturn(maybeRun);
		
		service.executeRun(runMessage);
		
		run.setPhase(RunPhase.COMPLETE);
		verify(runRepository, times(1)).saveAndFlush(run);
	}
	
	@Test
	public void testRunFreestyleJob() throws Exception {
		
		FreestyleJobBean job = new FreestyleJobBean();
		job.setId(123);
		job.setName("job");
		job.setDockerImage("busybox:1.31");
		job.setCommand(new String[] {"sh", "-c", "echo \"Hello world\""});
		
		FreestyleRunBean run = new FreestyleRunBean();
		run.setId(456);
		run.setJob(job);
		run.setPhase(RunPhase.IN_PROGRESS);
		run.setTriggerAt(new Date(0L));
		run.setCompleteAt(new Date(1000L));
		
		DockerRunCmdResultDTO result = service.runFreestyleJob(run);
		assertEquals(result.getOutput(), "Hello world\n");
	}
	
	@Test
	public void testRunGitJobDefault() throws Exception {
		testRunGitJobHelloWorld("git-default");
	}

	@Test
	public void testRunGitJobCustomizedBasedir() throws Exception {
		testRunGitJobHelloWorld("git-customized-basedir");
	}
	
	@Test
	public void testRunGitJobCustomizedDockerfile() throws Exception {
		testRunGitJobHelloWorld("git-customized-dockerfile");
	}
	
	private void testRunGitJobHelloWorld(String resourceName) throws Exception {
		
		GitJobBean job = new GitJobBean();
		job.setId(123);
		job.setName("job");
		job.setRemoteOrigin("git@github.com:dummy/dummy.git");
		job.setConfigFilepath("restfulci.yml");
		
		GitBranchRunBean run = new GitBranchRunBean();
		run.setId(456);
		run.setJob(job);
		run.setBranchName("master");
		run.setPhase(RunPhase.IN_PROGRESS);
		run.setTriggerAt(new Date(0L));
		run.setCompleteAt(new Date(1000L));
		
		doAnswer(new Answer<Void>() {
			public Void answer(InvocationOnMock invocation) {
				Path localRepoPath = (Path) invocation.getArguments()[1];
				try {
					FileUtils.copyDirectory(
							new File(getClass().getClassLoader().getResource("docker-run-service-test/"+resourceName).getFile()),
							localRepoPath.toFile());
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}
		 }).when(remoteGitRepository).copyToLocal(any(GitRunBean.class), any(Path.class));
		
		DockerRunCmdResultDTO result = service.runGitJob(run);
		assertEquals(result.getOutput(), "Hello world\n");
	}
	
	@Test
	public void testRunCommand() throws Exception {
		
		String[] command = new String[] {"sh", "-c", "echo \"Hello world\""};
		DockerRunCmdResultDTO result = service.runCommand("busybox:1.31", Arrays.asList(command));
		
		assertEquals(result.getExitCode(), 0);
		assertEquals(result.getOutput(), "Hello world\n");
	}
}
