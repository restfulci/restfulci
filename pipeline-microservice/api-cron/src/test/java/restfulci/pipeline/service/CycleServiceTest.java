package restfulci.pipeline.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import restfulci.pipeline.dao.CycleRepository;
import restfulci.pipeline.dao.RemoteRunRepository;
import restfulci.pipeline.domain.CycleBean;
import restfulci.pipeline.domain.CycleStatus;
import restfulci.pipeline.domain.InputBean;
import restfulci.pipeline.domain.InputMapBean;
import restfulci.pipeline.domain.ParameterBean;
import restfulci.pipeline.domain.ParameterMapBean;
import restfulci.pipeline.domain.PipelineBean;
import restfulci.pipeline.domain.ReferredJobBean;
import restfulci.pipeline.domain.ReferredRunBean;
import restfulci.pipeline.domain.ReferredRunStatus;
import restfulci.pipeline.domain.RemoteRunBean;
import restfulci.pipeline.dto.CycleDTO;
import restfulci.pipeline.exception.CycleDataException;
import restfulci.pipeline.exception.IncompleteParameterLinkException;
import restfulci.pipeline.exception.RunTriggerException;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class CycleServiceTest {

	@Autowired private CycleService service;
	
	@MockBean private CycleRepository cycleRepository;
	@MockBean private RemoteRunRepository remoteRunRepository;
	@MockBean private PipelineService pipelineService;
	
	@Test
	public void testTriggerCycleWithCorrectParameter() throws Exception {
		
		PipelineBean pipeline = new PipelineBean();
		ParameterBean parameter = new ParameterBean();
		parameter.setName("INCLUDE");
		pipeline.addParameter(parameter);
		
		ReferredJobBean referredJob = new ReferredJobBean();
		referredJob.setOriginalJobId(123);
		referredJob.setPipeline(pipeline);
		pipeline.addReferredJob(referredJob);
		
		ParameterMapBean parameterMap = new ParameterMapBean();
		parameterMap.setReferredJob(referredJob);
		parameterMap.setParameter(parameter);
		parameterMap.setRemoteName("REMOTE_INCLUDE");
		parameterMap.setOptional(false);
		
		referredJob.addParameterMap(parameterMap);
		
		given(pipelineService.getPipeline(456)).willReturn(pipeline);
		
		CycleDTO cycleDTO = new CycleDTO();
		cycleDTO.put("INCLUDE", "foo");
		
		service.triggerCycle(456, cycleDTO);
		
		ArgumentCaptor<CycleBean> cycleCaptor = ArgumentCaptor.forClass(CycleBean.class);
		verify(cycleRepository, times(1)).saveAndFlush(cycleCaptor.capture());
		CycleBean triggeredCycle = cycleCaptor.getValue();
		
		assertEquals(triggeredCycle.getInputs().size(), 1);
		InputBean input = new ArrayList<>(triggeredCycle.getInputs()).get(0);
		assertEquals(input.getName(), "INCLUDE");
		assertEquals(input.getValue(), "foo");
		
		assertEquals(triggeredCycle.getReferredRuns().size(), 1);
		ReferredRunBean referredRun = new ArrayList<>(triggeredCycle.getReferredRuns()).get(0);
		assertEquals(referredRun.getOriginalJobId(), 123);
		
		assertEquals(referredRun.getInputMaps().size(), 1);
		InputMapBean inputMap = new ArrayList<>(referredRun.getInputMaps()).get(0);
		assertEquals(inputMap.getReferredRun(), referredRun);
		assertEquals(inputMap.getInput(), input);
		assertEquals(inputMap.getRemoteName(), "REMOTE_INCLUDE");
	}
	
	@Test
	public void testTriggerCycleErrorsOutIfExtraInput() throws Exception {
		
		PipelineBean pipeline = new PipelineBean();
		given(pipelineService.getPipeline(456)).willReturn(pipeline);
		
		CycleDTO cycleDTO = new CycleDTO();
		cycleDTO.put("EXCLUDE", "foo");
		
		Assertions.assertThrows(CycleDataException.class, () -> {
			service.triggerCycle(456, cycleDTO);
		});
	}
	
	@Test
	public void testTriggerCycleErrorsOutIfMissInput() throws Exception {
		
		PipelineBean pipeline = new PipelineBean();
		ParameterBean parameter = new ParameterBean();
		parameter.setName("INCLUDE");
		pipeline.addParameter(parameter);
		given(pipelineService.getPipeline(456)).willReturn(pipeline);
		
		Assertions.assertThrows(CycleDataException.class, () -> {
			service.triggerCycle(456, new CycleDTO());
		});
	}
	
	@Test
	public void testTriggerCycleErrorsOutIfInputNotUnderChoices() throws Exception {
		
		PipelineBean pipeline = new PipelineBean();
		ParameterBean parameter = new ParameterBean();
		parameter.setName("ENV");
		parameter.setChoices(new String[] {"testing", "staging", "production"});
		pipeline.addParameter(parameter);
		given(pipelineService.getPipeline(456)).willReturn(pipeline);
		
		CycleDTO cycleDTO = new CycleDTO();
		cycleDTO.put("ENV", "development");
		
		Assertions.assertThrows(CycleDataException.class, () -> {
			service.triggerCycle(456, cycleDTO);
		});
	}
	
	@Test
	public void testTriggerCycleErrorOutIfMandatoryParameterIsNotLinked() throws Exception {
		
		PipelineBean pipeline = new PipelineBean();
		
		ReferredJobBean referredJob = new ReferredJobBean();
		referredJob.setOriginalJobId(123);
		referredJob.setPipeline(pipeline);
		pipeline.addReferredJob(referredJob);
		
		ParameterMapBean parameterMap = new ParameterMapBean();
		parameterMap.setReferredJob(referredJob);
		parameterMap.setRemoteName("REMOTE_UNLINKED");
		parameterMap.setOptional(false);
		
		referredJob.addParameterMap(parameterMap);
		
		given(pipelineService.getPipeline(456)).willReturn(pipeline);
		
		Assertions.assertThrows(IncompleteParameterLinkException.class, () -> {
			service.triggerCycle(456, new CycleDTO());
		});
	}
	
	@Test
	public void testTriggerCycleErrorOutIfOptionalParameterIsNotLinked() throws Exception {
		
		PipelineBean pipeline = new PipelineBean();
		
		ReferredJobBean referredJob = new ReferredJobBean();
		referredJob.setOriginalJobId(123);
		referredJob.setPipeline(pipeline);
		pipeline.addReferredJob(referredJob);
		
		ParameterMapBean parameterMap = new ParameterMapBean();
		parameterMap.setReferredJob(referredJob);
		parameterMap.setRemoteName("REMOTE_UNLINKED");
		parameterMap.setOptional(true);
		
		referredJob.addParameterMap(parameterMap);
		
		given(pipelineService.getPipeline(456)).willReturn(pipeline);
		
		service.triggerCycle(456, new CycleDTO());
		
		ArgumentCaptor<CycleBean> cycleCaptor = ArgumentCaptor.forClass(CycleBean.class);
		verify(cycleRepository, times(1)).saveAndFlush(cycleCaptor.capture());
		CycleBean triggeredCycle = cycleCaptor.getValue();
		
		assertEquals(triggeredCycle.getInputs().size(), 0);
	}
	
	@Test
	public void testUpdateCycleStartNotStartedYetWithoutDependency() throws Exception {
		
		ReferredRunBean referredRun = new ReferredRunBean();
		referredRun.setOriginalJobId(123);
		referredRun.setStatus(ReferredRunStatus.NOT_STARTED_YET);
		
		CycleBean cycle = new CycleBean();
		cycle.setId(456);
		cycle.setStatus(CycleStatus.IN_PROGRESS);
		cycle.addReferredRun(referredRun);
		
		referredRun.setCycle(cycle);
		
		RemoteRunBean remoteRun = new RemoteRunBean();
		remoteRun.setId(789);
		remoteRun.setStatus("IN_PROGRESS");
		given(remoteRunRepository.triggerRun(123, new HashMap<String, String>())).willReturn(remoteRun);
		
		service.updateCycle(cycle);
		
		assertEquals(referredRun.getStatus(), ReferredRunStatus.IN_PROGRESS);
		assertEquals(cycle.getStatus(), CycleStatus.IN_PROGRESS);
		verify(remoteRunRepository, never()).getRun(123, 789);
	}
	
	@Test
	public void testTriggerRunErrorsOutResultCycleFail() throws Exception {
		
		ReferredRunBean referredRun = new ReferredRunBean();
		referredRun.setOriginalJobId(123);
		referredRun.setStatus(ReferredRunStatus.NOT_STARTED_YET);
		
		CycleBean cycle = new CycleBean();
		cycle.setId(456);
		cycle.setStatus(CycleStatus.IN_PROGRESS);
		cycle.addReferredRun(referredRun);
		
		when(remoteRunRepository.triggerRun(123, new HashMap<String, String>())).thenThrow(RunTriggerException.class);
		
		service.updateCycle(cycle);
		
		assertEquals(referredRun.getStatus(), ReferredRunStatus.ERROR);
		assertEquals(cycle.getStatus(), CycleStatus.FAIL);
		assertNotNull(cycle.getCompleteAt());
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
		assertNotNull(cycle.getCompleteAt());
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
		assertNotNull(cycle.getCompleteAt());
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
		verify(remoteRunRepository, never()).triggerRun(234, new HashMap<String, String>());
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
		given(remoteRunRepository.triggerRun(234, new HashMap<String, String>())).willReturn(remoteDownstreamRun);
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
	public void testUpdateCycleDownstreamIsSkippedIfUpstreamError() throws Exception {
		
		ReferredRunBean upstreamRun = new ReferredRunBean();
		upstreamRun.setOriginalJobId(123);
		upstreamRun.setStatus(ReferredRunStatus.ERROR);
		
		ReferredRunBean downstreamRun = new ReferredRunBean();
		downstreamRun.setOriginalJobId(234);
		downstreamRun.setStatus(ReferredRunStatus.NOT_STARTED_YET);
		downstreamRun.addReferredUpstreamRun(upstreamRun);
		
		CycleBean cycle = new CycleBean();
		cycle.setId(456);
		cycle.setStatus(CycleStatus.FAIL);
		cycle.addReferredRun(upstreamRun);
		cycle.addReferredRun(downstreamRun);
		
		service.updateCycle(cycle);
		
		assertEquals(downstreamRun.getStatus(), ReferredRunStatus.SKIP);
		verify(remoteRunRepository, never()).triggerRun(234, new HashMap<String, String>());
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
		verify(remoteRunRepository, never()).triggerRun(234, new HashMap<String, String>());
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
		given(remoteRunRepository.triggerRun(234, new HashMap<String, String>())).willReturn(remoteDownstreamRun);
		given(remoteRunRepository.getRun(234, 890)).willReturn(remoteDownstreamRun);
		
		service.updateCycle(cycle);
		service.updateCycle(cycle);
		
		return downstreamRun.getStatus();
	}
	
	@Test
	public void testUpdateCycleReferredRunWithParameter() throws Exception {
		
		CycleBean cycle = new CycleBean();
		cycle.setId(456);
		cycle.setStatus(CycleStatus.IN_PROGRESS);
		
		ReferredRunBean referredRun = new ReferredRunBean();
		referredRun.setOriginalJobId(123);
		referredRun.setStatus(ReferredRunStatus.NOT_STARTED_YET);
		referredRun.setCycle(cycle);
		
		InputBean input = new InputBean();
		input.setName("ENV");
		input.setValue("stage");
		input.setCycle(cycle);
		
		cycle.addReferredRun(referredRun);
		cycle.addInput(input);
		
		InputMapBean inputMap = new InputMapBean();
		inputMap.setReferredRun(referredRun);
		inputMap.setInput(input);
		inputMap.setRemoteName("REMOTE_ENV");
		
		referredRun.addInputMap(inputMap);
		
		RemoteRunBean remoteRun = new RemoteRunBean();
		remoteRun.setId(789);
		remoteRun.setStatus("IN_PROGRESS");
		given(remoteRunRepository.triggerRun(eq(123), ArgumentMatchers.<Map<String, String>>any())).willReturn(remoteRun);
		
		service.updateCycle(cycle);
		
		ArgumentCaptor<HashMap<String, String>> parameterValuePairCaptor = ArgumentCaptor.forClass(HashMap.class);
		verify(remoteRunRepository, times(1)).triggerRun(any(Integer.class), parameterValuePairCaptor.capture());
		
		HashMap<String, String> parameterValuePair = parameterValuePairCaptor.getValue();
		assertEquals(parameterValuePair.size(), 1);
		assertTrue(parameterValuePair.containsKey("REMOTE_ENV"));
		assertEquals(parameterValuePair.get("REMOTE_ENV"), "stage");
	}
}
