package restfulci.master.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.commons.io.input.NullInputStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import restfulci.master.service.JobService;
import restfulci.master.service.RunService;

@WebMvcTest(RunsController.class)
public class RunsControllerTest {

	@Autowired private MockMvc mockMvc;

	@MockBean private JobService jobService;
	@MockBean private RunService runService;

	@Test
	public void testRunResultContentType() throws Exception {
		when(runService.getRunResultStream(any(Integer.class))).thenReturn(new NullInputStream(0));
		this.mockMvc.perform(get("/jobs/1/runs/1/results/1"))
//				.andDo(print())
				.andExpect(status().isOk())
				.andReturn().getResponse().getHeader("Content type");
	}
}
