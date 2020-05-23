package restfulci.pipeline.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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
	public void testUpdateCycleStartNotStartedYetWithoutDependency() throws Exception {
		
		ReferredRunBean referredRun = new ReferredRunBean();
		referredRun.setOriginalJobId(123);
		referredRun.setStatus(ReferredRunStatus.NOT_STARTED_YET);
		
		CycleBean cycle = new CycleBean();
		cycle.setId(456);
		cycle.setStatus(CycleStatus.IN_PROGRESS);
		cycle.addReferredRun(referredRun);
		
		RemoteRunBean remoteRun = new RemoteRunBean();
		remoteRun.setId(789);
		remoteRun.setStatus("IN_PROGRESS");
		given(remoteRunRepository.triggerRun(123)).willReturn(remoteRun);
		
		service.updateCycle(cycle);
		
		assertEquals(referredRun.getStatus(), ReferredRunStatus.IN_PROGRESS);
		assertEquals(cycle.getStatus(), CycleStatus.IN_PROGRESS);
		verify(remoteRunRepository, never()).getRun(123, 789);
	}
	
	@Test
	public void testUpdateCycleSucceedInProgressWithoutDependency() throws Exception {
		
		ReferredRunBean referredRun = new ReferredRunBean();
		referredRun.setOriginalJobId(123);
		referredRun.setOriginalRunId(789);
		referredRun.setStatus(ReferredRunStatus.IN_PROGRESS);
		
		CycleBean cycle = new CycleBean();
		cycle.setId(456);
		cycle.setStatus(CycleStatus.IN_PROGRESS);
		cycle.addReferredRun(referredRun);
		
		RemoteRunBean remoteRun = new RemoteRunBean();
		remoteRun.setId(789);
		remoteRun.setStatus("SUCCEED");
		given(remoteRunRepository.getRun(123, 789)).willReturn(remoteRun);
		
		service.updateCycle(cycle);
		
		assertEquals(referredRun.getStatus(), ReferredRunStatus.SUCCEED);
		assertEquals(cycle.getStatus(), CycleStatus.SUCCEED);
	}
	
	@Test
	public void testUpdateCycleFailInProgressWithoutDependency() throws Exception {
		
		ReferredRunBean referredRun = new ReferredRunBean();
		referredRun.setOriginalJobId(123);
		referredRun.setOriginalRunId(789);
		referredRun.setStatus(ReferredRunStatus.IN_PROGRESS);
		
		CycleBean cycle = new CycleBean();
		cycle.setId(456);
		cycle.setStatus(CycleStatus.IN_PROGRESS);
		cycle.addReferredRun(referredRun);
		
		RemoteRunBean remoteRun = new RemoteRunBean();
		remoteRun.setId(789);
		remoteRun.setStatus("FAIL");
		given(remoteRunRepository.getRun(123, 789)).willReturn(remoteRun);
		
		service.updateCycle(cycle);
		
		assertEquals(referredRun.getStatus(), ReferredRunStatus.FAIL);
		assertEquals(cycle.getStatus(), CycleStatus.FAIL);
	}
	
	@Test
	public void testUpdateCycleDownstreamIsNotTriggeredIfUpstreamIsInProgress() throws Exception {
		
		ReferredRunBean upstreamRun = new ReferredRunBean();
		upstreamRun.setOriginalJobId(123);
		upstreamRun.setOriginalRunId(789);
		upstreamRun.setStatus(ReferredRunStatus.IN_PROGRESS);
		
		ReferredRunBean downstreamRun = new ReferredRunBean();
		downstreamRun.setOriginalJobId(234);
		downstreamRun.setStatus(ReferredRunStatus.NOT_STARTED_YET);
		downstreamRun.addReferredUpstreamRun(upstreamRun);
		
		CycleBean cycle = new CycleBean();
		cycle.setId(456);
		cycle.setStatus(CycleStatus.IN_PROGRESS);
		cycle.addReferredRun(upstreamRun);
		cycle.addReferredRun(downstreamRun);
		
		RemoteRunBean remoteUpstreamRun = new RemoteRunBean();
		remoteUpstreamRun.setId(789);
		remoteUpstreamRun.setStatus("IN_PROGRESS");
		given(remoteRunRepository.getRun(123, 789)).willReturn(remoteUpstreamRun);
		
		service.updateCycle(cycle);
		
		assertEquals(upstreamRun.getStatus(), ReferredRunStatus.IN_PROGRESS);
		assertEquals(downstreamRun.getStatus(), ReferredRunStatus.NOT_STARTED_YET);
		assertEquals(cycle.getStatus(), CycleStatus.IN_PROGRESS);
		verify(remoteRunRepository, never()).triggerRun(234);
	}
	
	@Test
	public void testUpdateCycleDownstreamIsTriggeredIfUpstreamSucceed() throws Exception {
		
		ReferredRunBean upstreamRun = new ReferredRunBean();
		upstreamRun.setOriginalJobId(123);
		upstreamRun.setOriginalRunId(789);
		upstreamRun.setStatus(ReferredRunStatus.IN_PROGRESS);
		
		ReferredRunBean downstreamRun = new ReferredRunBean();
		downstreamRun.setOriginalJobId(234);
		downstreamRun.setStatus(ReferredRunStatus.NOT_STARTED_YET);
		downstreamRun.addReferredUpstreamRun(upstreamRun);
		
		CycleBean cycle = new CycleBean();
		cycle.setId(456);
		cycle.setStatus(CycleStatus.IN_PROGRESS);
		cycle.addReferredRun(upstreamRun);
		cycle.addReferredRun(downstreamRun);
		
		RemoteRunBean remoteUpstreamRun = new RemoteRunBean();
		remoteUpstreamRun.setId(789);
		remoteUpstreamRun.setStatus("SUCCEED");
		given(remoteRunRepository.getRun(123, 789)).willReturn(remoteUpstreamRun);
		
		RemoteRunBean remoteDownstreamRun = new RemoteRunBean();
		remoteDownstreamRun.setId(890);
		remoteDownstreamRun.setStatus("IN_PROGRESS");
		given(remoteRunRepository.triggerRun(234)).willReturn(remoteDownstreamRun);
		given(remoteRunRepository.getRun(234, 890)).willReturn(remoteDownstreamRun);
		
		/*
		 * It is unpredictable weather upstream or downstream run is iterated first
		 * in the loop. If upstream comes first, one loop is okay, and the 2nd loop
		 * will call `remoteRunRepository.getRun(123, 890)`. Otherwise if downstream
		 * comes first, 2 loops is needed, and `remoteRunRepository.getRun(123, 890)`
		 * will not be called.
		 */
		service.updateCycle(cycle);
		service.updateCycle(cycle);
		
		assertEquals(upstreamRun.getStatus(), ReferredRunStatus.SUCCEED);
		assertEquals(downstreamRun.getStatus(), ReferredRunStatus.IN_PROGRESS);
		assertEquals(cycle.getStatus(), CycleStatus.IN_PROGRESS);
	}
	
	@Test
	public void testUpdateCycleDownstreamIsSkippedIfUpstreamFail() throws Exception {
		assertSkipDownstream("FAIL");
	}
	
	@Test
	public void testUpdateCycleDownstreamIsSkippedIfUpstreamIsAborted() throws Exception {
		assertSkipDownstream("ABORT");
	}
	
	@Test
	public void testUpdateCycleDownstreamIsSkippedIfUpstreamIsSkipped() throws Exception {
		assertSkipDownstream("SKIP");
	}
		
	private void assertSkipDownstream(String upstreamRemoteStatus) throws Exception {
		
		ReferredRunBean upstreamRun = new ReferredRunBean();
		upstreamRun.setOriginalJobId(123);
		upstreamRun.setOriginalRunId(789);
		upstreamRun.setStatus(ReferredRunStatus.IN_PROGRESS);
		
		ReferredRunBean downstreamRun = new ReferredRunBean();
		downstreamRun.setOriginalJobId(234);
		downstreamRun.setStatus(ReferredRunStatus.NOT_STARTED_YET);
		downstreamRun.addReferredUpstreamRun(upstreamRun);
		
		CycleBean cycle = new CycleBean();
		cycle.setId(456);
		cycle.setStatus(CycleStatus.IN_PROGRESS);
		cycle.addReferredRun(upstreamRun);
		cycle.addReferredRun(downstreamRun);

		RemoteRunBean remoteUpstreamRun = new RemoteRunBean();
		remoteUpstreamRun.setId(789);
		remoteUpstreamRun.setStatus(upstreamRemoteStatus);
		given(remoteRunRepository.getRun(123, 789)).willReturn(remoteUpstreamRun);
		
		service.updateCycle(cycle);
		service.updateCycle(cycle);
		
		assertEquals(downstreamRun.getStatus(), ReferredRunStatus.SKIP);
		verify(remoteRunRepository, never()).triggerRun(234);
	}
	
	@Test
	public void testUpdateCycleDownstreamIsTriggeredIfAllUpstreamsSucceed() throws Exception {
		assertEquals(
				updateAndGetDownstreamRunWithUpstreamsRemoteStatus("SUCCEED", "SUCCEED"),
				ReferredRunStatus.IN_PROGRESS);
	}
	
	@Test
	public void testUpdateCycleDownstreamIsNotTriggeredIfSomeUpstreamIsStillInProgress() throws Exception {
		assertEquals(
				updateAndGetDownstreamRunWithUpstreamsRemoteStatus("SUCCEED", "IN_PROGRESS"),
				ReferredRunStatus.NOT_STARTED_YET);
	}
	
	@Test
	public void testUpdateCycleDownstreamIsSkippedIfOneUpstreamFail() throws Exception {
		assertEquals(
				updateAndGetDownstreamRunWithUpstreamsRemoteStatus("SUCCEED", "FAIL"),
				ReferredRunStatus.SKIP);
	}
		
	private ReferredRunStatus updateAndGetDownstreamRunWithUpstreamsRemoteStatus(
			String remoteStatus1, String remoteStatus2) throws Exception {
	
		ReferredRunBean upstreamRun1 = new ReferredRunBean();
		upstreamRun1.setOriginalJobId(123);
		upstreamRun1.setOriginalRunId(789);
		upstreamRun1.setStatus(ReferredRunStatus.IN_PROGRESS);
		
		ReferredRunBean upstreamRun2 = new ReferredRunBean();
		upstreamRun2.setOriginalJobId(124);
		upstreamRun2.setOriginalRunId(790);
		upstreamRun2.setStatus(ReferredRunStatus.IN_PROGRESS);
		
		ReferredRunBean downstreamRun = new ReferredRunBean();
		downstreamRun.setOriginalJobId(234);
		downstreamRun.setStatus(ReferredRunStatus.NOT_STARTED_YET);
		downstreamRun.addReferredUpstreamRun(upstreamRun1);
		downstreamRun.addReferredUpstreamRun(upstreamRun2);
		
		CycleBean cycle = new CycleBean();
		cycle.setId(456);
		cycle.setStatus(CycleStatus.IN_PROGRESS);
		cycle.addReferredRun(upstreamRun1);
		cycle.addReferredRun(upstreamRun2);
		cycle.addReferredRun(downstreamRun);
		
		RemoteRunBean remoteUpstreamRun1 = new RemoteRunBean();
		remoteUpstreamRun1.setId(789);
		remoteUpstreamRun1.setStatus(remoteStatus1);
		given(remoteRunRepository.getRun(123, 789)).willReturn(remoteUpstreamRun1);
		
		RemoteRunBean remoteUpstreamRun2 = new RemoteRunBean();
		remoteUpstreamRun2.setId(790);
		remoteUpstreamRun2.setStatus(remoteStatus2);
		given(remoteRunRepository.getRun(124, 790)).willReturn(remoteUpstreamRun2);
		
		RemoteRunBean remoteDownstreamRun = new RemoteRunBean();
		remoteDownstreamRun.setId(890);
		remoteDownstreamRun.setStatus("IN_PROGRESS");
		given(remoteRunRepository.triggerRun(234)).willReturn(remoteDownstreamRun);
		given(remoteRunRepository.getRun(234, 890)).willReturn(remoteDownstreamRun);
		
		service.updateCycle(cycle);
		service.updateCycle(cycle);
		
		return downstreamRun.getStatus();
	}
}
