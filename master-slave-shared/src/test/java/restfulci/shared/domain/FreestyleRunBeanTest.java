package restfulci.shared.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FreestyleRunBeanTest {

	@Test
	public void testCorrectDisplayNestedJobJson() throws Exception {
		
		FreestyleJobBean job = new FreestyleJobBean();
		job.setId(123);
		job.setName("job");
		job.setDockerImage("busybox:1.31");
		job.setCommand(new String[] {"echo", "0"});
		
		FreestyleRunBean run = new FreestyleRunBean();
		run.setId(456);
		run.setJob(job);
		run.setPhase(RunPhase.IN_PROGRESS);
		run.setTriggerAt(new Date(0L));
		run.setCompleteAt(new Date(1000L));
		run.setExitCode(0);
		
		ObjectMapper mapper = new ObjectMapper();
		assertEquals(
				mapper.writeValueAsString(run),
				"{\"id\":456,"
				+ "\"job\":{\"id\":123,\"name\":\"job\",\"dockerImage\":\"busybox:1.31\",\"command\":[\"echo\",\"0\"],\"type\":\"FREESTYLE\"},"
				+ "\"phase\":\"IN_PROGRESS\","
				+ "\"triggerAt\":\"1970-01-01 12:00:00\","
				+ "\"completeAt\":\"1970-01-01 12:00:01\","
				+ "\"exitCode\":0,"
				+ "\"type\":\"FREESTYLE\"}");
		assertEquals(
				mapper.writeValueAsString(job),
				"{\"id\":123,\"name\":\"job\",\"dockerImage\":\"busybox:1.31\",\"command\":[\"echo\",\"0\"],\"type\":\"FREESTYLE\"}");
	}
}
