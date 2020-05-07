package restfulci.master.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class GitJobUserJourneyIT {
	
	@Autowired private MockMvc mockMvc;
	
	private ObjectMapper objectMapper;
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
	}

	@Test
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
		
		final String branchName = "master";
		Map<String, String> runData = new HashMap<String, String>();
		runData.put("branchName", branchName);
		runData.put("ENV", "staging");
		
		/*
		 * curl -X POST -H "Content-Type: application/json" --data '{"branchName": "master"}' localhost:8881/jobs/1/runs
		 */
		Map<?, ?> triggeredRun = objectMapper.readValue(
				mockMvc.perform(post("/jobs/"+jobId+"/runs")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectWriter.writeValueAsString(runData)))
						.andExpect(status().isOk())
						.andReturn().getResponse().getContentAsString(),
				Map.class);
		assertEquals(triggeredRun.get("phase"), "IN_PROGRESS");
		assertEquals(triggeredRun.get("branchName"), branchName);
		assertEquals(triggeredRun.get("type"), "GIT");
		assertEquals(
				objectMapper.convertValue(triggeredRun.get("job"), Map.class).get("type"), 
				"GIT");
		assertEquals(objectMapper.convertValue(triggeredRun.get("inputs"), List.class).size(), 1);
		assertEquals(
				objectMapper.convertValue(
						objectMapper.convertValue(triggeredRun.get("inputs"), List.class).get(0),
						Map.class).get("name"), 
				"ENV");
		
		Integer runId = (Integer)triggeredRun.get("id");
		Map<?, ?> queriedRun = objectMapper.readValue(
				mockMvc.perform(get("/jobs/"+jobId+"/runs/"+runId))
						.andExpect(status().isOk())
						.andReturn().getResponse().getContentAsString(), 
				Map.class);
		assertEquals(queriedRun.get("phase"), "IN_PROGRESS");
		
		mockMvc.perform(delete("/jobs/"+jobId))
				.andExpect(status().isOk());
	}
}
