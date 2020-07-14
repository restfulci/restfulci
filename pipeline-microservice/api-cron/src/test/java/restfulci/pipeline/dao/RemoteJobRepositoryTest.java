package restfulci.pipeline.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;

import com.fasterxml.jackson.databind.ObjectMapper;

import restfulci.pipeline.domain.RemoteJobBean;

@ExtendWith(SpringExtension.class)
@RestClientTest(RemoteJobRepository.class)
@AutoConfigureWebClient(registerRestTemplate=true)
public class RemoteJobRepositoryTest {
	
	@Autowired private RemoteJobRepository remoteJobRepository;
	@Autowired private MockRestServiceServer mockServer;
	
	private ObjectMapper objectMapper = new ObjectMapper();

	@Test
	public void testGetJobWithoutParamDefault() throws Exception {
		
		Map<String, Object> returnedParam = new HashMap<String, Object>();
		returnedParam.put("name", "ENV");
		List<Object> returnedParams = new ArrayList<Object>();
		returnedParams.add(returnedParam);
		
		Map<String, Object> returnedJob = new HashMap<String, Object>();
		returnedJob.put("id", 1);
		returnedJob.put("name", "job_name");
		returnedJob.put("type", "FREESTYLE");
		returnedJob.put("parameters", returnedParams);
		
		mockServer.expect(
				ExpectedCount.once(), 
				requestTo("/jobs/1"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withStatus(HttpStatus.OK)
				.contentType(MediaType.APPLICATION_JSON)
				.body(objectMapper.writeValueAsString(returnedJob))
				);
		
		RemoteJobBean queriedJob = remoteJobRepository.getJob(1);
		
		mockServer.verify();
		
		assertEquals(queriedJob.getId(), 1);
		assertEquals(queriedJob.getName(), "job_name");
		assertEquals(queriedJob.getType(), "FREESTYLE");
		assertEquals(queriedJob.getParameters().size(), 1);
		assertEquals(queriedJob.getParameters().get(0).getName(), "ENV");
		assertFalse(queriedJob.getParameters().get(0).isOptional());
	}
	
	@Test
	public void testGetJobWithParamDefault() throws Exception {
		
		Map<String, Object> returnedParam = new HashMap<String, Object>();
		returnedParam.put("name", "ENV");
		returnedParam.put("defaultValue", "stage");
		List<Object> returnedParams = new ArrayList<Object>();
		returnedParams.add(returnedParam);
		
		Map<String, Object> returnedJob = new HashMap<String, Object>();
		returnedJob.put("id", 1);
		returnedJob.put("name", "job_name");
		returnedJob.put("type", "FREESTYLE");
		returnedJob.put("parameters", returnedParams);
		
		mockServer.expect(
				ExpectedCount.once(), 
				requestTo("/jobs/1"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withStatus(HttpStatus.OK)
				.contentType(MediaType.APPLICATION_JSON)
				.body(objectMapper.writeValueAsString(returnedJob))
				);
		
		RemoteJobBean queriedJob = remoteJobRepository.getJob(1);
		
		mockServer.verify();
		
		assertEquals(queriedJob.getId(), 1);
		assertEquals(queriedJob.getName(), "job_name");
		assertEquals(queriedJob.getType(), "FREESTYLE");
		assertEquals(queriedJob.getParameters().size(), 1);
		assertEquals(queriedJob.getParameters().get(0).getName(), "ENV");
		assertTrue(queriedJob.getParameters().get(0).isOptional());
	}
}
