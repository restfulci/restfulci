package restfulci.master.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class RunBeanTest {

	@Test
	public void testCorrectDisplayNestedJobJson() throws Exception {
		
		FreestyleJobBean job = new FreestyleJobBean();
		job.setId(123);
		job.setName("job");
		job.setScript("echo 0");
		
		RunBean run = new RunBean();
		run.setId(456);
		run.setJob(job);
		run.setTriggerAt(new Date(0L));
		run.setCompleteAt(new Date(1000L));
		
		ObjectMapper mapper = new ObjectMapper();
		assertEquals(
				mapper.writeValueAsString(run),
				"{\"id\":456,"
				+ "\"job\":{\"id\":123,\"name\":\"job\",\"script\":\"echo 0\",\"type\":\"freestyle\"},"
				+ "\"triggerAt\":\"1970-01-01 12:00:00\","
				+ "\"completeAt\":\"1970-01-01 12:00:01\"}");
		assertEquals(
				mapper.writeValueAsString(job),
				"{\"id\":123,\"name\":\"job\",\"script\":\"echo 0\",\"type\":\"freestyle\"}");
	}
}
