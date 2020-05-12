package restfulci.pipeline.api;

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
		
		/*
		 * curl -X DELETE localhost:8881/pipelines/1
		 */
		mockMvc.perform(delete("/pipelines/"+pipelineId))
				.andExpect(status().isOk());
	}
}
