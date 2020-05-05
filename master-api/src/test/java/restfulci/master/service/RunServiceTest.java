package restfulci.master.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import restfulci.master.dto.RunDTO;
import restfulci.shared.dao.MinioRepository;
import restfulci.shared.dao.RunRepository;
import restfulci.shared.dao.RunResultRepository;
import restfulci.shared.domain.FreestyleJobBean;
import restfulci.shared.domain.FreestyleRunBean;
import restfulci.shared.domain.GitBranchRunBean;
import restfulci.shared.domain.GitCommitRunBean;
import restfulci.shared.domain.GitJobBean;
import restfulci.shared.domain.JobBean;
import restfulci.shared.domain.RunBean;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class RunServiceTest {

	@Autowired private RunService runService;
	
	@MockBean private RunRepository runRepository;
	@MockBean private RunResultRepository runResultRepository;
	@MockBean private MinioRepository minioRepository;
	
	/*
	 * Can't mock `AmqpTemplate`. But after `admin` is mocked, messages are not
	 * sending to RabbitMQ either. So should be fine.
	 */
	@MockBean private AmqpAdmin admin;
//	@MockBean private AmqpTemplate template;
	
	@Test
	public void testTriggerFreestyleRun() throws Exception {
		
		JobBean job = new FreestyleJobBean();
		
		RunDTO runDTO = new RunDTO();
		
		runService.triggerRun(job, runDTO);
		
		ArgumentCaptor<RunBean> runCaptor = ArgumentCaptor.forClass(RunBean.class);
		verify(runRepository, times(1)).saveAndFlush(runCaptor.capture());
		assertTrue(runCaptor.getValue() instanceof FreestyleRunBean);
	}
	
	@Test
	public void testTriggerGitBranchRun() throws Exception {
		
		JobBean job = new GitJobBean();
		
		RunDTO runDTO = new RunDTO();
		runDTO.setBranchName("master");
		
		runService.triggerRun(job, runDTO);
		
		ArgumentCaptor<RunBean> runCaptor = ArgumentCaptor.forClass(RunBean.class);
		verify(runRepository, times(1)).saveAndFlush(runCaptor.capture());
		assertTrue(runCaptor.getValue() instanceof GitBranchRunBean);
	}
	
	@Test
	public void testTriggerGitCommitRun() throws Exception {
		
		JobBean job = new GitJobBean();
		
		RunDTO runDTO = new RunDTO();
		runDTO.setCommitSha("0000000000000000000000000000000000000000");
		
		runService.triggerRun(job, runDTO);
		
		ArgumentCaptor<RunBean> runCaptor = ArgumentCaptor.forClass(RunBean.class);
		verify(runRepository, times(1)).saveAndFlush(runCaptor.capture());
		assertTrue(runCaptor.getValue() instanceof GitCommitRunBean);
	}
}
