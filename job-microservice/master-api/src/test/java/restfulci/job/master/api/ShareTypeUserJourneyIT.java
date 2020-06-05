package restfulci.job.master.api;

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
public class ShareTypeUserJourneyIT {
	
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
	public void testBlankJobDefinitionReturnsBadRequest() throws Exception {
		
		mockMvc.perform(post("/jobs")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectWriter.writeValueAsString(new HashMap<String, String>())))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testJobDefinitionWithDoesnotFitExistingJobTypesReturnsBadRequest() throws Exception {
		
		Map<String, Object> jobData = new HashMap<String, Object>();
		jobData.put("name", "it_job_name");
		
		mockMvc.perform(post("/jobs")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectWriter.writeValueAsString(jobData)))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testGetJobWithNonExistenceIdReturnsNotFound() throws Exception {
		
		mockMvc.perform(get("/jobs/123")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectWriter.writeValueAsString(new HashMap<String, String>())))
				.andExpect(status().isNotFound());
	}
}
