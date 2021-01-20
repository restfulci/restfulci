package restfulci.job.master.api;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import restfulci.job.master.MasterApplication;
import restfulci.job.master.config.KeycloakConfig;
import restfulci.job.master.config.OAuth2LoginConfig;
import restfulci.job.master.dto.JobDTO;
import restfulci.job.master.service.JobService;
import restfulci.job.shared.domain.FreestyleJobBean;
import restfulci.job.shared.domain.ParameterBean;

@WebMvcTest(JobsController.class)
@ContextConfiguration(classes={
		MasterApplication.class, 
		OAuth2LoginConfig.class,
		KeycloakConfig.class})
public class JobsControllerTest {

	@Autowired private MockMvc mockMvc;

	@MockBean private JobService jobService;
	
	private ObjectMapper objectMapper;
	private ObjectWriter objectWriter;
	
	@BeforeEach
	public void setUp() throws JsonProcessingException {
		objectMapper = new ObjectMapper();
		objectWriter = objectMapper.writer().withDefaultPrettyPrinter();
	}
	
	@Test
	@WithMockUser
	public void testAddJob() throws Exception {
		
		Map<String, Object> jobData = new HashMap<String, Object>();
		jobData.put("name", "job_name");
		jobData.put("dockerImage", "busybox:1.33");
		jobData.put("command", new String[]{"sh", "-c", "echo \"Hello world\""});
			
		this.mockMvc.perform(post("/jobs")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectWriter.writeValueAsString(jobData)))
				.andExpect(status().isOk());
		
		ArgumentCaptor<JobDTO> jobCaptor = ArgumentCaptor.forClass(JobDTO.class);
		verify(jobService, times(1)).createJob(jobCaptor.capture());
		assertEquals(jobCaptor.getValue().getName(), "job_name");
	}
	
	@Test
	@WithMockUser
	public void testAddParameterMinimal() throws Exception {
		
		Map<String, Object> parameterData = new HashMap<String, Object>();
		parameterData.put("name", "ENV");
	
		when(jobService.getJob(any(Integer.class))).thenReturn(new FreestyleJobBean());
		this.mockMvc.perform(post("/jobs/1/parameters")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectWriter.writeValueAsString(parameterData)))	
				.andExpect(status().isOk());
		
		ArgumentCaptor<ParameterBean> parameterCaptor = ArgumentCaptor.forClass(ParameterBean.class);
		verify(jobService, times(1)).addParameter(any(Integer.class), parameterCaptor.capture());
		assertEquals(parameterCaptor.getValue().getName(), "ENV");
		assertEquals(parameterCaptor.getValue().getDefaultValue(), null);
		assertArrayEquals(parameterCaptor.getValue().getChoices(), null);
	}

	@Test
	@WithMockUser
	public void testAddParameterFull() throws Exception {
		
		Map<String, Object> parameterData = new HashMap<String, Object>();
		parameterData.put("name", "ENV");
		parameterData.put("defaultValue", "dev");
		parameterData.put("choices", new String[]{"dev", "staging", "production"});
		
		when(jobService.getJob(any(Integer.class))).thenReturn(new FreestyleJobBean());
		this.mockMvc.perform(post("/jobs/1/parameters")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectWriter.writeValueAsString(parameterData)))	
				.andExpect(status().isOk());
		
		ArgumentCaptor<ParameterBean> parameterCaptor = ArgumentCaptor.forClass(ParameterBean.class);
		verify(jobService, times(1)).addParameter(any(Integer.class), parameterCaptor.capture());
		assertEquals(parameterCaptor.getValue().getName(), "ENV");
		assertEquals(parameterCaptor.getValue().getDefaultValue(), "dev");
		assertArrayEquals(parameterCaptor.getValue().getChoices(), new String[]{"dev", "staging", "production"});
	}
}
