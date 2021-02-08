package restfulci.pipeline.dao;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;

import com.fasterxml.jackson.databind.ObjectMapper;

import restfulci.pipeline.domain.RemoteRunBean;
import restfulci.pipeline.exception.RunTriggerException;

@ExtendWith(SpringExtension.class)
@RestClientTest(RemoteRunRepository.class)
@AutoConfigureWebClient(registerRestTemplate=true)
public class RemoteRunRepositoryTest {
	
	@Autowired private RemoteRunRepository remoteRunRepository;
	@Autowired private MockRestServiceServer mockServer;
	
	private ObjectMapper objectMapper = new ObjectMapper();
	
	private final String token = "foo";

	@Test
	public void testTriggerRun() throws Exception {
		
		Map<String, Object> returnedRun = new HashMap<String, Object>();
		returnedRun.put("id", 456);
		returnedRun.put("status", "IN_PROGRESS");
		returnedRun.put("exitCode", 0);
		
		mockServer.expect(
				ExpectedCount.once(), 
				requestTo("/jobs/123/runs"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(header(HttpHeaders.AUTHORIZATION, token))
				.andExpect(jsonPath("$.ENV", is("stage")))
				.andRespond(withStatus(HttpStatus.OK)
				.contentType(MediaType.APPLICATION_JSON)
				.body(objectMapper.writeValueAsString(returnedRun))
				);
		
		Map<String, String> parameterValuePair = new HashMap<String, String>();
		parameterValuePair.put("ENV", "stage");
		
		RemoteRunBean triggeredRun = remoteRunRepository.triggerRun(
				123, 
				parameterValuePair,
				token);
		
		mockServer.verify();
		
		assertEquals(triggeredRun.getId(), 456);
		assertEquals(triggeredRun.getStatus(), "IN_PROGRESS");
		assertEquals(triggeredRun.getExitCode(), 0);
	}
	
	@Test
	public void testGetRun() throws Exception {
		
		Map<String, Object> returnedRun = new HashMap<String, Object>();
		returnedRun.put("id", 456);
		returnedRun.put("status", "IN_PROGRESS");
		returnedRun.put("exitCode", 0);
		
		mockServer.expect(
				ExpectedCount.once(), 
				requestTo("/jobs/123/runs/456"))
				.andExpect(method(HttpMethod.GET))
				.andExpect(header(HttpHeaders.AUTHORIZATION, token))
				.andRespond(withStatus(HttpStatus.OK)
				.contentType(MediaType.APPLICATION_JSON)
				.body(objectMapper.writeValueAsString(returnedRun))
				);
		
		RemoteRunBean queriedRun = remoteRunRepository.getRun(
				123, 
				456,
				token);
		
		mockServer.verify();
		
		assertEquals(queriedRun.getId(), 456);
		assertEquals(queriedRun.getStatus(), "IN_PROGRESS");
		assertEquals(queriedRun.getExitCode(), 0);
	}
	
	@Test
	public void testTriggerRunBadRequest() throws Exception {
		
		mockServer.expect(
				ExpectedCount.once(), 
				requestTo("/jobs/123/runs"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(header(HttpHeaders.AUTHORIZATION, token))
				.andRespond(withStatus(HttpStatus.BAD_REQUEST)
				);
		
		assertThrows(RunTriggerException.class, () -> {
			remoteRunRepository.triggerRun(
					123, 
					new HashMap<String, String>(),
					token);
		});
	}
	
	@Test
	public void testTriggerRunInternalServerError() throws Exception {
			
		mockServer.expect(
				ExpectedCount.once(), 
				requestTo("/jobs/123/runs"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(header(HttpHeaders.AUTHORIZATION, token))
				.andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
				);
		
		assertThrows(RunTriggerException.class, () -> {
			remoteRunRepository.triggerRun(
					123, 
					new HashMap<String, String>(),
					token);
		});
	}
}
