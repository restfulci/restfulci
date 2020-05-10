package restfulci.shared.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ParameterBeanTest {

	@Test
	public void testValidation() {

		ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		
		ParameterBean validatedParameter = new ParameterBean();
		validatedParameter.setName("ENV_09");
		assertTrue(validator.validate(validatedParameter).isEmpty());
		
		ParameterBean invalidParameter = new ParameterBean();
		invalidParameter.setName("env");
		assertEquals(validator.validate(invalidParameter).size(), 1);
	}

	@Test
	public void testJsonParametersNotShowIfEmpty() throws Exception {
		
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
	public void testJsonShowParametersWithoutOptionalAttributes() throws Exception {
		
		GitJobBean job = new GitJobBean();
		job.setId(123);
		job.setName("job");
		job.setRemoteOrigin("git@github.com:dummy/dummy.git");
		job.setConfigFilepath(".restfulci.yml");
		
		ParameterBean simpleParameter = new ParameterBean();
		simpleParameter.setId(4);
		simpleParameter.setName("SIMPLE");
		job.addParameter(simpleParameter);
		
		ObjectMapper mapper = new ObjectMapper();
		assertEquals(
				mapper.writeValueAsString(job),
				"{\"id\":123,"
				+ "\"name\":\"job\","
				+ "\"parameters\":["
				+ "{\"id\":4,\"name\":\"SIMPLE\"}"
				+ "],"
				+ "\"remoteOrigin\":\"git@github.com:dummy/dummy.git\","
				+ "\"configFilepath\":\".restfulci.yml\","
				+ "\"type\":\"GIT\"}");
	}
	
	@Test
	public void testJsonShowParametersWithOptionalAttributes() throws Exception {
		
		GitJobBean job = new GitJobBean();
		job.setId(123);
		job.setName("job");
		job.setRemoteOrigin("git@github.com:dummy/dummy.git");
		job.setConfigFilepath(".restfulci.yml");
		
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
				+ "{\"id\":5,\"name\":\"COMPLEX\",\"defaultValue\":\"foo\",\"choices\":[\"foo\",\"bar\"]}"
				+ "],"
				+ "\"remoteOrigin\":\"git@github.com:dummy/dummy.git\","
				+ "\"configFilepath\":\".restfulci.yml\","
				+ "\"type\":\"GIT\"}");
	}
}
