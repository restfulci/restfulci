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
public class FreestyleJobUserJourneyIT {
	
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
						.content(objectWriter.writeValueAsString(runData)))
						.andExpect(status().isOk())
						.andReturn().getResponse().getContentAsString(),
				Map.class);
		assertEquals(triggeredRun.get("status"), "IN_PROGRESS");
		assertEquals(triggeredRun.get("type"), "FREESTYLE");
		assertEquals(
				objectMapper.convertValue(triggeredRun.get("job"), Map.class).get("type"),
				"FREESTYLE");
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
	public void testAInvalidJobReturnsBadRequest() throws Exception {
		
		mockMvc.perform(post("/jobs")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectWriter.writeValueAsString(new HashMap<String, String>())))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testParameteredJobRunReturnOkWithValidInputAndReturnBadRequestWithInvalidInput() throws Exception {
		
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
		
		Map<String, Object> runData = new HashMap<String, Object>();
		runData.put("MINUEND", 5);
		runData.put("SUBTRAHEND", 3);
		
		mockMvc.perform(post("/jobs/"+jobId+"/runs")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectWriter.writeValueAsString(runData)))
				.andExpect(status().isOk());
		
		mockMvc.perform(post("/jobs/"+jobId+"/runs")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectWriter.writeValueAsString(new HashMap<String, Object>())))
				.andExpect(status().isBadRequest());
		
		mockMvc.perform(delete("/jobs/"+jobId))
		.andExpect(status().isOk());
	}
}
