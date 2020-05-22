package restfulci.pipeline.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import restfulci.pipeline.dao.CycleRepository;
import restfulci.pipeline.dao.RemoteRunRepository;
import restfulci.pipeline.domain.CycleBean;
import restfulci.pipeline.domain.CycleStatus;
import restfulci.pipeline.domain.ReferredRunBean;
import restfulci.pipeline.domain.ReferredRunStatus;
import restfulci.pipeline.domain.RemoteRunBean;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class CycleServiceTest {

	@Autowired private CycleService service;
	
	@MockBean private CycleRepository cycleRepository;
	@MockBean private RemoteRunRepository remoteRunRepository;
	@MockBean private PipelineService pipelineService;
	
	@Test
	public void testUpdateCycleStartNotStartedYetWithoutDependency() throws Exception{
		
		ReferredRunBean referredRun = new ReferredRunBean();
		referredRun.setOriginalJobId(123);
		referredRun.setStatus(ReferredRunStatus.NOT_STARTED_YET);
		
		CycleBean cycle = new CycleBean();
		cycle.setId(456);
		cycle.setStatus(CycleStatus.IN_PROGRESS);
		cycle.addReferredRun(referredRun);
		
		Optional<CycleBean> maybeCycle = Optional.of(cycle);
		given(cycleRepository.findById(456)).willReturn(maybeCycle);
		
		RemoteRunBean remoteRun = new RemoteRunBean();
		remoteRun.setId(789);
		remoteRun.setStatus("IN_PROGRESS");
		
		given(remoteRunRepository.triggerRun(123)).willReturn(remoteRun);
		given(remoteRunRepository.getRun(123, 789)).willReturn(remoteRun);
		
		service.updateCycle(456);
		
		assertEquals(referredRun.getStatus(), ReferredRunStatus.IN_PROGRESS);
	}
	
	@Test
	public void testUpdateCycleSucceedInProgressWithoutDependency() throws Exception{
		
		ReferredRunBean referredRun = new ReferredRunBean();
		referredRun.setOriginalJobId(123);
		referredRun.setOriginalRunId(789);
		referredRun.setStatus(ReferredRunStatus.IN_PROGRESS);
		
		CycleBean cycle = new CycleBean();
		cycle.setId(456);
		cycle.setStatus(CycleStatus.IN_PROGRESS);
		cycle.addReferredRun(referredRun);
		
		Optional<CycleBean> maybeCycle = Optional.of(cycle);
		given(cycleRepository.findById(456)).willReturn(maybeCycle);
		
		RemoteRunBean remoteRun = new RemoteRunBean();
		remoteRun.setId(789);
		remoteRun.setStatus("SUCCESS");
		
		given(remoteRunRepository.getRun(123, 789)).willReturn(remoteRun);
		
		service.updateCycle(456);
		
		assertEquals(referredRun.getStatus(), ReferredRunStatus.SUCCESS);
		assertEquals(cycle.getStatus(), CycleStatus.SUCCESS);
	}
	
	@Test
	public void testUpdateCycleFailInProgressWithoutDependency() throws Exception{
		
		ReferredRunBean referredRun = new ReferredRunBean();
		referredRun.setOriginalJobId(123);
		referredRun.setOriginalRunId(789);
		referredRun.setStatus(ReferredRunStatus.IN_PROGRESS);
		
		CycleBean cycle = new CycleBean();
		cycle.setId(456);
		cycle.setStatus(CycleStatus.IN_PROGRESS);
		cycle.addReferredRun(referredRun);
		
		Optional<CycleBean> maybeCycle = Optional.of(cycle);
		given(cycleRepository.findById(456)).willReturn(maybeCycle);
		
		RemoteRunBean remoteRun = new RemoteRunBean();
		remoteRun.setId(789);
		remoteRun.setStatus("FAIL");
		
		given(remoteRunRepository.getRun(123, 789)).willReturn(remoteRun);
		
		service.updateCycle(456);
		
		assertEquals(referredRun.getStatus(), ReferredRunStatus.FAIL);
		assertEquals(cycle.getStatus(), CycleStatus.FAIL);
	}
}
