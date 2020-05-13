package restfulci.master.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.input.NullInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import restfulci.master.dto.RunDTO;
import restfulci.master.service.JobService;
import restfulci.master.service.RunService;
import restfulci.shared.domain.FreestyleJobBean;

@WebMvcTest(RunsController.class)
public class RunsControllerTest {

	@Autowired private MockMvc mockMvc;

	@MockBean private JobService jobService;
	@MockBean private RunService runService;
	
	private ObjectMapper objectMapper;
	private ObjectWriter objectWriter;
	
	@BeforeEach
	public void setUp() throws JsonProcessingException {
		objectMapper = new ObjectMapper();
		objectWriter = objectMapper.writer().withDefaultPrettyPrinter();
	}
	
	@Test
	public void testTriggerRunValidInput() throws Exception {
		
		Map<String, String> runData = new HashMap<String, String>();
		runData.put("branchName", "master");
		runData.put("ENV", "staging");
		
		when(jobService.getJob(any(Integer.class))).thenReturn(new FreestyleJobBean());
		this.mockMvc.perform(post("/jobs/1/runs")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectWriter.writeValueAsString(runData)))	
				.andExpect(status().isOk());
		
		ArgumentCaptor<RunDTO> runCaptor = ArgumentCaptor.forClass(RunDTO.class);
		verify(runService, times(1)).triggerRun(any(Integer.class), runCaptor.capture());
		assertEquals(runCaptor.getValue().get("branchName"), "master");
		assertEquals(runCaptor.getValue().get("ENV"), "staging");
	}
	
	@Test
	public void testTriggerRunInvalidInput() throws Exception {
		
		Map<String, Object> runData = new HashMap<String, Object>();
		runData.put("INTEGER", 1);
		runData.put("ARRAY", new String[]{"foo", "bar"});
		
		when(jobService.getJob(any(Integer.class))).thenReturn(new FreestyleJobBean());
		this.mockMvc.perform(post("/jobs/1/runs")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectWriter.writeValueAsString(runData)))	
				.andExpect(status().isBadRequest());
	}

	@Test
	public void testRunResultContentType() throws Exception {
		when(runService.getRunResultStream(any(Integer.class))).thenReturn(new NullInputStream(0));
		this.mockMvc.perform(get("/jobs/1/runs/1/results/1"))
//				.andDo(print())
				.andExpect(status().isOk())
				.andReturn().getResponse().getHeader("Content type");
	}
}
