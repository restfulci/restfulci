package restfulci.slave.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import restfulci.shared.dao.RunRepository;
import restfulci.shared.domain.DockerRunCmdResultBean;
import restfulci.shared.domain.FreestyleJobBean;
import restfulci.shared.domain.JobBean;
import restfulci.shared.domain.RunBean;
import restfulci.shared.domain.RunMessageBean;

import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class DockerRunServiceTest {
	
	@Autowired private DockerRunService service;
	
	@MockBean private RunRepository runRepository;
	
	@Test
	public void testExecuteFreestyleRun() throws Exception {
		
		RunMessageBean runMessage = new RunMessageBean();
		runMessage.setJobId(123);
		runMessage.setRunId(456);
		
		FreestyleJobBean job = new FreestyleJobBean();
		job.setId(123);
		job.setName("job");
		job.setDockerImage("busybox");
		job.setCommand(new String[] {"sh", "-c", "echo \"Hello world\""});
		
		RunBean run = new RunBean();
		run.setId(456);
		run.setJob(job);
		run.setTriggerAt(new Date(0L));
		run.setCompleteAt(new Date(1000L));
		Optional<RunBean> maybeRun = Optional.of(run);
		
		given(runRepository.findById(456)).willReturn(maybeRun);
		
		service.executeRun(runMessage);
	}

	@Test
	public void testRunCommand() throws Exception {
		
		String[] command = new String[] {"sh", "-c", "echo \"Hello world\""};
		DockerRunCmdResultBean result = service.runCommand("busybox", command);
		
		assertEquals(result.getExitCode(), 0);
		assertEquals(result.getOutput(), "Hello world\n");
	}
}
