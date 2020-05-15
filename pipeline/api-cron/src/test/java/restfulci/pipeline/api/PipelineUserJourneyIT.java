package restfulci.pipeline.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
public class PipelineUserJourneyIT {

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
		
		final String pipelineName = "it_pipeline_name";
		Map<String, Object> pipelineData = new HashMap<String, Object>();
		pipelineData.put("name", pipelineName);
		
		/*
		 * curl -X POST -H "Content-Type: application/json" --data '{"name": "manual_pipeline_name"}' localhost:8881/pipelines
		 */
		Map<?, ?> createdPipeline = objectMapper.readValue(
				mockMvc.perform(post("/pipelines")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectWriter.writeValueAsString(pipelineData)))
						.andExpect(status().isOk())
						.andReturn().getResponse().getContentAsString(),
				Map.class);
		assertEquals(createdPipeline.get("name"), pipelineName);
		
		Integer pipelineId = (Integer)createdPipeline.get("id");
		
		Map<?, ?> queriedPipeline = objectMapper.readValue(
				mockMvc.perform(get("/pipelines/"+pipelineId))
						.andExpect(status().isOk())
						.andReturn().getResponse().getContentAsString(),
				Map.class);
		assertEquals(queriedPipeline.get("name"), pipelineName);
		
		Map<String, Object> referredJobData = new HashMap<String, Object>();
		referredJobData.put("originalJobId", 123);
		
		Map<?, ?> referredJobAddedPipeline = objectMapper.readValue(
				mockMvc.perform(post("/pipelines/"+pipelineId+"/referred-jobs")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectWriter.writeValueAsString(referredJobData)))
						.andExpect(status().isOk())
						.andReturn().getResponse().getContentAsString(),
				Map.class);
		assertEquals(objectMapper.convertValue(referredJobAddedPipeline.get("referredJobs"), List.class).size(), 1);
		Map<?, ?> referredJob = objectMapper.convertValue(
				objectMapper.convertValue(referredJobAddedPipeline.get("referredJobs"), List.class).get(0),
				Map.class);
		assertEquals(referredJob.get("originalJobId"), 123);
		Integer referredJobId = (Integer)referredJob.get("id");
		
		Map<String, Object> anotherReferredJobData = new HashMap<String, Object>();
		anotherReferredJobData.put("originalJobId", 456);
		
		Map<?, ?> anotherReferredJobAddedPipeline = objectMapper.readValue(
				mockMvc.perform(post("/pipelines/"+pipelineId+"/referred-jobs")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectWriter.writeValueAsString(anotherReferredJobData)))
						.andExpect(status().isOk())
						.andReturn().getResponse().getContentAsString(),
				Map.class);
		assertEquals(objectMapper.convertValue(anotherReferredJobAddedPipeline.get("referredJobs"), List.class).size(), 2);
		Map<?, ?> anotherReferredJob = new HashMap<String, Object>();
		for (Object obj : objectMapper.convertValue(anotherReferredJobAddedPipeline.get("referredJobs"), List.class)) {
			Map<?, ?> iteratedReferredJob = objectMapper.convertValue(obj, Map.class);
			if (((Integer)iteratedReferredJob.get("originalJobId")).equals(456)) {
				anotherReferredJob = iteratedReferredJob;
			}
		}
		assertEquals(anotherReferredJob.get("originalJobId"), 456);
		Integer anotherReferredJobId = (Integer)anotherReferredJob.get("id");
		
		Map<?, ?> linkedUpstreamPipeline = objectMapper.readValue(
				mockMvc.perform(put("/pipelines/"+pipelineId+"/referred-jobs/"+referredJobId+"/referred-upstream-jobs/"+anotherReferredJobId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectWriter.writeValueAsString(anotherReferredJobData)))
//						.andDo(print())
						.andExpect(status().isOk())
						.andReturn().getResponse().getContentAsString(),
				Map.class);
		for (Object obj : objectMapper.convertValue(linkedUpstreamPipeline.get("referredJobs"), List.class)) {
			Map<?, ?> iteratedReferredJob = objectMapper.convertValue(obj, Map.class);
			if (((Integer)iteratedReferredJob.get("originalJobId")).equals(123)) {
				assertEquals(objectMapper.convertValue(iteratedReferredJob.get("referredUpstreamJobs"), List.class).size(), 1);
				assertEquals((Integer)objectMapper.convertValue(
						objectMapper.convertValue(iteratedReferredJob.get("referredUpstreamJobs"), List.class).get(0),
						Map.class).get("originalJobId"), 456);
			}
			if (((Integer)iteratedReferredJob.get("originalJobId")).equals(456)) {
				assertFalse(iteratedReferredJob.containsKey("referredUpstreamJobs"));
			}
		}
		
		Map<?, ?> cycle = objectMapper.readValue(
				mockMvc.perform(post("/pipelines/"+pipelineId+"/cycles")
						.contentType(MediaType.APPLICATION_JSON))
						.andDo(print())
						.andExpect(status().isOk())
						.andReturn().getResponse().getContentAsString(),
				Map.class);
		assertEquals((Integer)objectMapper.convertValue(cycle.get("pipeline"), Map.class).get("id"), pipelineId);
		assertEquals(objectMapper.convertValue(cycle.get("referredRuns"), List.class).size(), 2);
		
		/*
		 * curl -X DELETE localhost:8881/pipelines/1
		 */
		mockMvc.perform(delete("/pipelines/"+pipelineId))
				.andExpect(status().isOk());
	}
}
