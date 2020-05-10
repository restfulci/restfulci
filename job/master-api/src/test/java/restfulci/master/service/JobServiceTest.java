package restfulci.master.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import restfulci.master.dto.JobDTO;
import restfulci.shared.dao.JobRepository;
import restfulci.shared.domain.FreestyleJobBean;
import restfulci.shared.domain.GitJobBean;
import restfulci.shared.domain.JobBean;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class JobServiceTest {

	@Autowired private JobService jobService;
	
	@MockBean private JobRepository jobRepository;
	
	@Test
	public void testCreateFreestyleJob() throws Exception {
		
		JobDTO jobDTO = new JobDTO();
		jobDTO.setName("job_name");
		jobDTO.setDockerImage("busybox:1.31");
		jobDTO.setCommand(new String[]{"sh", "-c", "echo \"Hello world\""});
		
		jobService.createJob(jobDTO);
		
		ArgumentCaptor<JobBean> jobCaptor = ArgumentCaptor.forClass(JobBean.class);
		verify(jobRepository, times(1)).saveAndFlush(jobCaptor.capture());
		assertTrue(jobCaptor.getValue() instanceof FreestyleJobBean);
		assertEquals(jobCaptor.getValue().getName(), "job_name");
	}
	
	@Test
	public void testCreateGitJob() throws Exception {
		
		JobDTO jobDTO = new JobDTO();
		jobDTO.setName("job_name");
		jobDTO.setRemoteOrigin("git@github.com:dummy/dummy.git");
		jobDTO.setConfigFilepath("restfulci.yml");
		
		jobService.createJob(jobDTO);
		
		ArgumentCaptor<JobBean> jobCaptor = ArgumentCaptor.forClass(JobBean.class);
		verify(jobRepository, times(1)).saveAndFlush(jobCaptor.capture());
		assertTrue(jobCaptor.getValue() instanceof GitJobBean);
		assertEquals(jobCaptor.getValue().getName(), "job_name");
	}
}
