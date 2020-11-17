package restfulci.job.master.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

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
public class GitJobUserJourneyIT {
	
	@Autowired private MockMvc mockMvc;
	@Autowired private UserRepository userRepository;
	
	private ObjectMapper objectMapper;
	private ObjectWriter objectWriter;
	
	private final String thirteenYearsExpToken = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICIxV0UwbXBwcEdMeDZUYzVGMXRFVW1KS0UzdGtIX2NnNmRvSGhMRzdUVkVzIn0.eyJleHAiOjIwMzc1OTAyNDMsImlhdCI6MTYwNTU5MDI0MywianRpIjoiYTFmMmRhYTMtMWI3Yi00NzJkLWEyYjktNjgwNDM5NTlmNWY2IiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4ODgwL2F1dGgvcmVhbG1zL3Jlc3RmdWxjaSIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiJhODMyMWE4Yi01ZmU2LTRhYTktODYxMi1kM2VkMjlhZjI4NTMiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJqb2ItbWljcm9zZXJ2aWNlIiwic2Vzc2lvbl9zdGF0ZSI6ImVlNDdmMTYyLTU4ZDUtNDcyMS1iZTQ2LTFmYzhmMGYxY2JlNSIsImFjciI6IjEiLCJhbGxvd2VkLW9yaWdpbnMiOlsiKiJdLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwicHJlZmVycmVkX3VzZXJuYW1lIjoidGVzdC11c2VyIn0.GWzpeybEiFxXVQcSy-mqUdS1PmpvjKGcMEm77tUg0v_N56z5qOV3I0__8-bSvohOIlkcIXnwuXRkIuDFI6kKC0NHJzbFewoCoPPfrd6hEyQ9urnmEjqpe70Z6Gg2y24Z9WEk9NFXPWpRvXVnBOHtOBzxHGdIB0wqjlBlepXmLFxnm9BK1vkWQI3QDUKPt6HaNNtLbVEcY7jbAWutIHNLBdu66clEXxAwq7uSPeG_bzH0zL2qwzlzxFipPx6rRQwmaqzjU9Jzr6Qm47Et1UCS_xWfG5cjhMunenbCsoK0rdKOCUxD_lAVz1HCksxaXkoOc-L0_KjDBFZHVANoKKDdaA";
	
	@BeforeEach
	public void setUp() throws JsonProcessingException {
		objectMapper = new ObjectMapper();
		
		/*
		 * `DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES` is for HATEOAS `_link`.
		 */
		objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); 
		
		objectWriter = objectMapper.writer().withDefaultPrettyPrinter();
	}
	
	@AfterEach
	public void tearDown() {
		for (UserBean user : userRepository.findByAuthId("a8321a8b-5fe6-4aa9-8612-d3ed29af2853")) {
			userRepository.delete(user);
		}
	}

	@Test
	@WithMockUser
	public void testUserJourney() throws Exception {
		
		final String jobName = "it_git_job_name";
		Map<String, String> jobData = new HashMap<String, String>();
		jobData.put("name", jobName);
		jobData.put("remoteOrigin", "git@github.com:dummy/dummy.git");
		jobData.put("configFilepath", ".restfulci.yml");
		
		/*
		 * curl -X POST -H "Content-Type: application/json" --data '{"name": "manual_job_name", "remoteOrigin": "git@github.com:dummy/dummy.git", "configFilepath": ".restfulci.yml"}' localhost:8881/jobs 
		 */
		Map<?, ?> createdJob = objectMapper.readValue(
				mockMvc.perform(post("/jobs")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectWriter.writeValueAsString(jobData)))
						.andExpect(status().isOk())
						.andReturn().getResponse().getContentAsString(),
				Map.class);
		assertEquals(createdJob.get("name"), jobName);
		assertEquals(createdJob.get("type"), "GIT");
		
		Integer jobId = (Integer)createdJob.get("id");
		Map<?, ?> queriedJob = objectMapper.readValue(
				mockMvc.perform(get("/jobs/"+jobId))
						.andExpect(status().isOk())
						.andReturn().getResponse().getContentAsString(), 
				Map.class);
		assertEquals(queriedJob.get("name"), jobName);
		assertEquals(queriedJob.get("type"), "GIT");
		
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
		assertEquals(parameterAddedJob.get("type"), "GIT");
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
		
		final String branchName = "master";
		Map<String, String> runData = new HashMap<String, String>();
		runData.put("branchName", branchName);
		runData.put("ENV", "staging");
		runData.put("ANOTHER", "foo");
		
		/*
		 * curl -X POST -H "Content-Type: application/json" --data '{"branchName": "master"}' localhost:8881/jobs/1/runs
		 */
		Map<?, ?> triggeredRun = objectMapper.readValue(
				mockMvc.perform(post("/jobs/"+jobId+"/runs")
						.contentType(MediaType.APPLICATION_JSON)
						.header(HttpHeaders.AUTHORIZATION, thirteenYearsExpToken)
						.content(objectWriter.writeValueAsString(runData)))
						.andExpect(status().isOk())
						.andReturn().getResponse().getContentAsString(),
				Map.class);
		assertEquals(triggeredRun.get("status"), "IN_PROGRESS");
		assertEquals(triggeredRun.get("branchName"), branchName);
		assertEquals(triggeredRun.get("type"), "GIT");
		assertEquals(
				objectMapper.convertValue(triggeredRun.get("job"), Map.class).get("type"), 
				"GIT");
		assertEquals(objectMapper.convertValue(triggeredRun.get("inputs"), List.class).size(), 2);
		assertThat(
				Arrays.asList(new String[] {"ENV", "ANOTHER"}).contains(
					objectMapper.convertValue(
							objectMapper.convertValue(triggeredRun.get("inputs"), List.class).get(0),
							Map.class).get("name")));
		
		Integer runId = (Integer)triggeredRun.get("id");
		Map<?, ?> queriedRun = objectMapper.readValue(
				mockMvc.perform(get("/jobs/"+jobId+"/runs/"+runId))
						.andExpect(status().isOk())
						.andReturn().getResponse().getContentAsString(), 
				Map.class);
		assertEquals(queriedRun.get("status"), "IN_PROGRESS");
		
		mockMvc.perform(delete("/jobs/"+jobId))
				.andExpect(status().isOk());
	}
	
	@Test
	@WithMockUser
	public void testRunReturnBadRequestWithJobTypeMismatchedInput() throws Exception {
		
		final String jobName = "it_git_job_name";
		Map<String, String> jobData = new HashMap<String, String>();
		jobData.put("name", jobName);
		jobData.put("remoteOrigin", "git@github.com:dummy/dummy.git");
		jobData.put("configFilepath", ".restfulci.yml");
		
		Map<?, ?> createdJob = objectMapper.readValue(
				mockMvc.perform(post("/jobs")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectWriter.writeValueAsString(jobData)))
						.andExpect(status().isOk())
						.andReturn().getResponse().getContentAsString(),
				Map.class);
		assertEquals(createdJob.get("name"), jobName);
		assertEquals(createdJob.get("type"), "GIT");
		
		Integer jobId = (Integer)createdJob.get("id");
		
		mockMvc.perform(post("/jobs/"+jobId+"/runs")
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, thirteenYearsExpToken)
				.content(objectWriter.writeValueAsString(new HashMap<String, Object>())))
				.andExpect(status().isBadRequest());
		
		mockMvc.perform(delete("/jobs/"+jobId))
				.andExpect(status().isOk());
	}
}
