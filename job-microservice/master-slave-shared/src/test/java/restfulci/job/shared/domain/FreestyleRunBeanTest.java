package restfulci.job.shared.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import restfulci.job.shared.domain.FreestyleJobBean;
import restfulci.job.shared.domain.FreestyleRunBean;
import restfulci.job.shared.domain.RunStatus;

public class FreestyleRunBeanTest {

	@Test
	public void testCorrectDisplayNestedJobJson() throws Exception {
		
		FreestyleJobBean job = new FreestyleJobBean();
		job.setId(123);
		job.setName("job");
		job.setDockerImage("busybox:1.31");
		job.setCommand(new String[] {"echo", "0"});
		
		UserBean user = new UserBean();
		user.setId(456);
		user.setAuthId("0000-0000");
		user.setUsername("username");
		
		FreestyleRunBean run = new FreestyleRunBean();
		run.setId(456);
		run.setJob(job);
		run.setUser(user);
		run.setStatus(RunStatus.IN_PROGRESS);
		run.setTriggerAt(new Date(0L));
		run.setCompleteAt(new Date(1000L));
		run.setExitCode(0);
		
		ObjectMapper mapper = new ObjectMapper();
		assertEquals(
				mapper.writeValueAsString(run),
				"{\"id\":456,"
				+ "\"job\":{\"id\":123,\"name\":\"job\",\"dockerImage\":\"busybox:1.31\",\"command\":[\"echo\",\"0\"],\"type\":\"FREESTYLE\"},"
				+ "\"user\":{\"id\":456,\"authId\":\"0000-0000\",\"username\":\"username\"},"
				+ "\"status\":\"IN_PROGRESS\","
				+ "\"triggerAt\":\"1970-01-01 12:00:00\","
				+ "\"completeAt\":\"1970-01-01 12:00:01\","
				+ "\"exitCode\":0,"
				+ "\"runResults\":[],"
				+ "\"type\":\"FREESTYLE\","
				+ "\"durationInSecond\":1}");
		assertEquals(
				mapper.writeValueAsString(job),
				"{\"id\":123,"
				+ "\"name\":\"job\","
				+ "\"dockerImage\":\"busybox:1.31\","
				+ "\"command\":[\"echo\",\"0\"],"
				+ "\"type\":\"FREESTYLE\"}");
	}
}
