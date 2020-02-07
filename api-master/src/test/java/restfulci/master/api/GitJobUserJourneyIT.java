package restfulci.master.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
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
@ActiveProfiles("local")
public class GitJobUserJourneyIT {
	
@Autowired MockMvc mockMvc;
	
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
		
		final String jobName = "it_job_name";
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
		Map<?, ?> queriedJob = objectMapper.readValue(
				mockMvc.perform(get("/jobs/"+jobId))
						.andExpect(status().isOk())
						.andReturn().getResponse().getContentAsString(), 
				Map.class);
		assertEquals(queriedJob.get("name"), jobName);
		assertEquals(queriedJob.get("type"), "GIT");
		
		final String branchName = "master";
		Map<String, String> runData = new HashMap<String, String>();
		runData.put("branchName", branchName);
		
		Map<?, ?> triggeredRun = objectMapper.readValue(
				mockMvc.perform(post("/jobs/"+jobId+"/runs")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectWriter.writeValueAsString(runData)))
						.andExpect(status().isOk())
						.andReturn().getResponse().getContentAsString(),
				Map.class);
		assertEquals(triggeredRun.get("branchName"), branchName);
		assertEquals(triggeredRun.get("type"), "GIT");
		
		mockMvc.perform(delete("/jobs/"+jobId))
				.andExpect(status().isOk());
	}
}
