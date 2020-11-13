package restfulci.job.master.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.net.HttpHeaders;

import restfulci.job.shared.dao.UserRepository;
import restfulci.job.shared.domain.UserBean;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class FreestyleJobUserJourneyIT {
	
	@Autowired private MockMvc mockMvc;
	@Autowired private RestTemplate restTemplate;
	@Autowired UserRepository userRepository;
	
	private MockRestServiceServer mockServer;
	private ObjectMapper objectMapper = new ObjectMapper();
	private ObjectWriter objectWriter;
	
	@BeforeEach
	public void setUp() throws JsonProcessingException {
		objectMapper = new ObjectMapper();
		
		/*
		 * `DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES` is for HATEOAS `_link`.
		 */
		objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		objectWriter = objectMapper.writer().withDefaultPrettyPrinter();
		
		mockServer = MockRestServiceServer.createServer(restTemplate);
		
		Map<String, Object> keyCloakUserinfo = new HashMap<String, Object>();
		keyCloakUserinfo.put("sub", "0000-0000");
		keyCloakUserinfo.put("email_verified", false);
		keyCloakUserinfo.put("preferred_username", "bar-user");
		
		mockServer.expect(ExpectedCount.once(), 
				requestTo("http://localhost:8880/auth/realms/restfulci/protocol/openid-connect/userinfo"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withStatus(HttpStatus.OK)
				.contentType(MediaType.APPLICATION_JSON)
				.body(objectMapper.writeValueAsString(keyCloakUserinfo))
			);
	}
	
	@AfterEach
	public void tearDown() {
	
		for (UserBean user : userRepository.findByAuthId("0000-0000")) {
			userRepository.delete(user);
		}
	}

	@Test
	@WithMockUser
	public void testUserJourney() throws Exception {
		
		final String jobName = "it_freestyle_job_name";
		Map<String, Object> jobData = new HashMap<String, Object>();
		jobData.put("name", jobName);
		jobData.put("dockerImage", "busybox:1.31");
		jobData.put("command", new String[] {"sh", "-c", "echo \"Hello world\""});
		
		/*
		 * curl -X POST -H "Content-Type: application/json" --data '{"name": "manual_freestyle_job_name", "dockerImage": "busybox:1.31", "command": ["sh", "-c", "echo \"Hello world\""]}' localhost:8881/jobs
		 */
		Map<?, ?> createdJob = objectMapper.readValue(
				mockMvc.perform(post("/jobs")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectWriter.writeValueAsString(jobData)))
						.andExpect(status().isOk())
						.andReturn().getResponse().getContentAsString(),
				Map.class);
		assertEquals(createdJob.get("name"), jobName);
		assertEquals(createdJob.get("type"), "FREESTYLE");
		
		Integer jobId = (Integer)createdJob.get("id");
		
		Map<?, ?> queriedJob = objectMapper.readValue(
				mockMvc.perform(get("/jobs/"+jobId))
						.andExpect(status().isOk())
						.andReturn().getResponse().getContentAsString(),
				Map.class);
		assertEquals(queriedJob.get("name"), jobName);
		assertEquals(queriedJob.get("type"), "FREESTYLE");
		
		List<?> queriedJobs = objectMapper.readValue(
				mockMvc.perform(get("/jobs?page=1&size=10"))
						.andExpect(status().isOk())
						.andReturn().getResponse().getContentAsString(),
				List.class);
		Map<?, ?> firstQueriedJob = objectMapper.convertValue(queriedJobs.get(0), Map.class);
		assertEquals(firstQueriedJob.get("name"), jobName);
		assertEquals(firstQueriedJob.get("type"), "FREESTYLE");
		
		Map<String, Object> parameterData = new HashMap<String, Object>();
		parameterData.put("name", "ENV");
		parameterData.put("defaultValue", "staging");
		parameterData.put("choices", new String[] {"staging", "production"});
		
		Map<?, ?> parameterAddedJob = objectMapper.readValue(
				mockMvc.perform(post("/jobs/"+jobId+"/parameters")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectWriter.writeValueAsString(parameterData)))
						.andExpect(status().isOk())
						.andReturn().getResponse().getContentAsString(),
				Map.class);
		assertEquals(parameterAddedJob.get("name"), jobName);
		assertEquals(parameterAddedJob.get("type"), "FREESTYLE");
		assertEquals(objectMapper.convertValue(parameterAddedJob.get("parameters"), List.class).size(), 1);
		assertEquals(
				objectMapper.convertValue(
						objectMapper.convertValue(parameterAddedJob.get("parameters"), List.class).get(0),
						Map.class).get("name"), 
				"ENV");
		
		Map<String, Object> anotherParameterData = new HashMap<String, Object>();
		anotherParameterData.put("name", "ANOTHER");
		
		Map<?, ?> anotherParameterAddedJob = objectMapper.readValue(
				mockMvc.perform(post("/jobs/"+jobId+"/parameters")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectWriter.writeValueAsString(anotherParameterData)))
						.andExpect(status().isOk())
						.andReturn().getResponse().getContentAsString(),
				Map.class);
		assertEquals(objectMapper.convertValue(anotherParameterAddedJob.get("parameters"), List.class).size(), 2);
		
		/*
		 * curl -X POST -H "Content-Type: application/json" --data '{"ENV": "staging", "ANOTHER", "foo"}' localhost:8881/jobs/1/runs
		 */
		Map<String, Object> runData = new HashMap<String, Object>();
		runData.put("ENV", "staging");
		runData.put("ANOTHER", "foo");
		
		Map<?, ?> triggeredRun = objectMapper.readValue(
				mockMvc.perform(post("/jobs/"+jobId+"/runs")
						.contentType(MediaType.APPLICATION_JSON)
						.header(HttpHeaders.AUTHORIZATION, "foo")
						.content(objectWriter.writeValueAsString(runData)))
						.andExpect(status().isOk())
						.andReturn().getResponse().getContentAsString(),
				Map.class);
		assertEquals(triggeredRun.get("status"), "IN_PROGRESS");
		assertEquals(triggeredRun.get("type"), "FREESTYLE");
		assertEquals(
				objectMapper.convertValue(triggeredRun.get("job"), Map.class).get("type"),
				"FREESTYLE");
		assertEquals(
				objectMapper.convertValue(triggeredRun.get("user"), Map.class).get("username"),
				"bar-user");
		assertEquals(objectMapper.convertValue(triggeredRun.get("inputs"), List.class).size(), 2);
		assertThat(
				Arrays.asList(new String[] {"ENV", "ANOTHER"}).contains(
					objectMapper.convertValue(
							objectMapper.convertValue(triggeredRun.get("inputs"), List.class).get(0),
							Map.class).get("name")));
		
		/*
		 * curl -X GET -H "Content-Type: application/json" localhost:8881/jobs/1/runs/1
		 */
		Integer runId = (Integer)triggeredRun.get("id");
		Map<?, ?> queriedRun = objectMapper.readValue(
				mockMvc.perform(get("/jobs/"+jobId+"/runs/"+runId))
						.andExpect(status().isOk())
						.andReturn().getResponse().getContentAsString(),
				Map.class);
		assertEquals(queriedRun.get("status"), "IN_PROGRESS");
		
		List<?> queriedRuns = objectMapper.readValue(
				mockMvc.perform(get("/jobs/"+jobId+"/runs?page=1&size=10"))
						.andExpect(status().isOk())
						.andReturn().getResponse().getContentAsString(),
				List.class);
		Map<?, ?> firstQueriedRun = objectMapper.convertValue(queriedRuns.get(0), Map.class);
		assertEquals(firstQueriedRun.get("status"), "IN_PROGRESS");
		
		/*
		 * curl -X GET -H "Content-Type: text/plain" -v localhost:8881/jobs/1/runs/1/console
		 */
		
		/*
		 * curl -X DELETE localhost:8881/jobs/1
		 */
		mockMvc.perform(delete("/jobs/"+jobId))
				.andExpect(status().isOk());
	}
	
	@Test
	@WithMockUser
	public void testRunReturnBadRequestWithJobTypeMismatchedInput() throws Exception {
		
		final String jobName = "it_freestyle_job_name";
		Map<String, Object> jobData = new HashMap<String, Object>();
		jobData.put("name", jobName);
		jobData.put("dockerImage", "busybox:1.31");
		jobData.put("command", new String[] {"sh", "-c", "echo \"Hello world\""});
		
		Map<?, ?> createdJob = objectMapper.readValue(
				mockMvc.perform(post("/jobs")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectWriter.writeValueAsString(jobData)))
						.andExpect(status().isOk())
						.andReturn().getResponse().getContentAsString(),
				Map.class);
		assertEquals(createdJob.get("name"), jobName);
		assertEquals(createdJob.get("type"), "FREESTYLE");
		
		Integer jobId = (Integer)createdJob.get("id");
		
		Map<String, Object> runData = new HashMap<String, Object>();
		runData.put("branchName", "master");
		
		mockMvc.perform(post("/jobs/"+jobId+"/runs")
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, "foo")
				.content(objectWriter.writeValueAsString(runData)))
				.andExpect(status().isBadRequest());
		
		mockMvc.perform(delete("/jobs/"+jobId))
				.andExpect(status().isOk());
	}
	
	@Test
	@WithMockUser
	public void testParameteredRunReturnOkWithValidInput() throws Exception {
		
		Integer jobId = setupJobAndReturnJobId();
		
		Map<String, Object> runData = new HashMap<String, Object>();
		runData.put("MINUEND", 5);
		runData.put("SUBTRAHEND", 3);
		
		mockMvc.perform(post("/jobs/"+jobId+"/runs")
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, "foo")
				.content(objectWriter.writeValueAsString(runData)))
				.andExpect(status().isOk());
		
		tearDownJob(jobId);
	}
	
	@Test
	@WithMockUser
	public void testParameteredReturnBadRequestWithInvalidInput() throws Exception {
		
		Integer jobId = setupJobAndReturnJobId();
		
		mockMvc.perform(post("/jobs/"+jobId+"/runs")
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, "foo")
				.content(objectWriter.writeValueAsString(new HashMap<String, Object>())))
				.andExpect(status().isBadRequest());
		
		tearDownJob(jobId);
	}
	
	private Integer setupJobAndReturnJobId() throws Exception {
		
		final String jobName = "it_freestyle_job_name";
		Map<String, Object> jobData = new HashMap<String, Object>();
		jobData.put("name", jobName);
		jobData.put("dockerImage", "busybox:1.31");
		jobData.put("command", new String[] {"sh", "-c", "expr $MINUEND - $SUBTRAHEND"});
		
		Map<?, ?> createdJob = objectMapper.readValue(
				mockMvc.perform(post("/jobs")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectWriter.writeValueAsString(jobData)))
						.andExpect(status().isOk())
						.andReturn().getResponse().getContentAsString(),
				Map.class);
		assertEquals(createdJob.get("name"), jobName);
		assertEquals(createdJob.get("type"), "FREESTYLE");
		
		Integer jobId = (Integer)createdJob.get("id");
		
		Map<String, Object> minuendParameterData = new HashMap<String, Object>();
		minuendParameterData.put("name", "MINUEND");
		
		mockMvc.perform(post("/jobs/"+jobId+"/parameters")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectWriter.writeValueAsString(minuendParameterData)))
				.andExpect(status().isOk());
		
		Map<String, Object> subtraheadParameterData = new HashMap<String, Object>();
		subtraheadParameterData.put("name", "SUBTRAHEND");
		
		mockMvc.perform(post("/jobs/"+jobId+"/parameters")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectWriter.writeValueAsString(subtraheadParameterData)))
				.andExpect(status().isOk());

		return jobId;
	}
	
	private void tearDownJob(Integer jobId) throws Exception {
		
		mockMvc.perform(delete("/jobs/"+jobId))
				.andExpect(status().isOk());
	}
}
