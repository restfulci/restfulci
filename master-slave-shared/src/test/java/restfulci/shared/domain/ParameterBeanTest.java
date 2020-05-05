package restfulci.shared.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ParameterBeanTest {

	@Test
	public void testParametersNotShowIfEmpty() throws Exception {
		
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
	
	@Test
	public void testShowParameters() throws Exception {
		
		GitJobBean job = new GitJobBean();
		job.setId(123);
		job.setName("job");
		job.setRemoteOrigin("git@github.com:dummy/dummy.git");
		job.setConfigFilepath(".restfulci.yml");
		
		ParameterBean simpleParameter = new ParameterBean();
		simpleParameter.setId(4);
		simpleParameter.setName("SIMPLE");
		job.addParameter(simpleParameter);
		
		ParameterBean complexParameter = new ParameterBean();
		complexParameter.setId(5);
		complexParameter.setName("COMPLEX");
		complexParameter.setDefaultValue("foo");
		complexParameter.setChoices(new String[]{"foo", "bar"});
		job.addParameter(complexParameter);
		
		ObjectMapper mapper = new ObjectMapper();
		assertEquals(
				mapper.writeValueAsString(job),
				"{\"id\":123,"
				+ "\"name\":\"job\","
				+ "\"parameters\":["
				+ "{\"id\":4,\"name\":\"SIMPLE\"},"
				+ "{\"id\":5,\"name\":\"COMPLEX\",\"defaultValue\":\"foo\",\"choices\":[\"foo\",\"bar\"]}"
				+ "],"
				+ "\"remoteOrigin\":\"git@github.com:dummy/dummy.git\","
				+ "\"configFilepath\":\".restfulci.yml\","
				+ "\"type\":\"GIT\"}");
	}
}
