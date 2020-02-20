package restfulci.master.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class GitJobBeanTest {

	@Test
	public void testTypeInJson() throws Exception {
		
		GitJobBean job = new GitJobBean();
		job.setId(123);
		job.setName("job");
		job.setRemoteOrigin("git@github.com:dummy/dummy.git");
		job.setConfigFilepath(".restfulci.yml");
		
		ObjectMapper mapper = new ObjectMapper();
		assertEquals(
				mapper.writeValueAsString(job),
				"{\"id\":123,"
				+ "\"name\":\"job\","
				+ "\"remoteOrigin\":\"git@github.com:dummy/dummy.git\","
				+ "\"configFilepath\":\".restfulci.yml\","
				+ "\"type\":\"GIT\"}");
	}
}
