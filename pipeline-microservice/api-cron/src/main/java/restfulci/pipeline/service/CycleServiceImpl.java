package restfulci.pipeline.service;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
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
import restfulci.pipeline.exception.BackendException;
import restfulci.pipeline.exception.CycleDataException;
import restfulci.pipeline.exception.IdNonExistenceException;
import restfulci.pipeline.exception.IncompleteParameterLinkException;
import restfulci.pipeline.exception.RunGetterException;
import restfulci.pipeline.exception.RunTriggerException;

@Service
@Transactional
@Slf4j
public class CycleServiceImpl implements CycleService {

	@Autowired private CycleRepository cycleRepository;
	@Autowired private RemoteRunRepository remoteRunRepository;

	@Autowired private PipelineService pipelineService;

	@Override
	public CycleBean getCycle(Integer cycleId) throws IOException {

		Optional<CycleBean> cycles = cycleRepository.findById(cycleId);
		if (cycles.isPresent()) {
			return cycles.get();
		} else {
			throw new IdNonExistenceException("Cycle ID does not exist yet.");
		}
	}

	@Override
	public CycleBean triggerCycle(Integer pipelineId, CycleDTO cycleDTO) throws IOException, BackendException {

		PipelineBean pipeline = pipelineService.getPipeline(pipelineId);
		log.info("Create cycle under pipeline {}", pipeline);

		CycleBean cycle = cycleDTO.toCycleBean();
		cycle.setPipeline(pipeline);
		
		cycle.fillInDefaultInput();
		cycle.validateInput();

		for (ReferredJobBean referredJob : pipeline.getReferredJobs()) {
			ReferredRunBean referredRun = new ReferredRunBean();
			referredRun.setOriginalJobId(referredJob.getOriginalJobId());
			
			for (ParameterMapBean parameterMap : referredJob.getParameterMaps()) {
				
				ParameterBean parameter = parameterMap.getParameter();
				if (parameter == null) {
					if (parameterMap.getOptional() == true) {
						continue;
					}
					else {
						throw new IncompleteParameterLinkException("Required remote parameter "+parameterMap.getRemoteName()+" is not linked to pipeline parameter.");
					}
				}
				
				InputBean input = cycle.getInput(parameter.getName());
				if (input == null) {
					/*
					 * This should not happen in general. One possibility for it to happen is
					 * the `parameterMaps` got updated after the pipeline input is setting up,
					 * but before this particular remote job is triggered. 
					 */
					throw new CycleDataException("Input "+parameterMap.getParameter().getName()+" is missing");
				}
				
				InputMapBean inputMap = new InputMapBean();
				inputMap.setReferredRun(referredRun);
				inputMap.setInput(input);
				inputMap.setRemoteName(parameterMap.getRemoteName());
				
				referredRun.addInputMap(inputMap);
			}

			referredRun.setCycle(cycle);
			cycle.addReferredRun(referredRun);
		}

		for (ReferredJobBean referredJob : pipeline.getReferredJobs()) {
			for (ReferredJobBean referredUpstreamJob : referredJob.getReferredUpstreamJobs()) {
				cycle.getReferredRunByOriginalJobId(referredJob.getOriginalJobId()).addReferredUpstreamRun(
						cycle.getReferredRunByOriginalJobId(referredUpstreamJob.getOriginalJobId()));
			}
		}

		return cycleRepository.saveAndFlush(cycle);
	}

	/*
	 * TODO:
	 * 
	 * So this logic is pulling job-microservice to get referred job updated.
	 * Alternatively, what we can do is in `job` of job-microservice, provide an
	 * optional callback URL that job-microservice can call when the job finished.
	 * Or similarly, job-microservice can be a publisher while pipeline-microservice
	 * is a subscriber to subscribe the news that a run has finished.
	 * 
	 * Another possibility is to let job-microservice to have some endpoint which we
	 * can check newly updated run status (as a whole site). So this update doesn't
	 * need to be per cycle; rather, it can get a list of all referred run from the
	 * whole job-microservice, and update accordingly.
	 * 
	 * We should probably decide which is a better approach.
	 */
	@Override
	public CycleBean updateCycle(CycleBean cycle) throws IOException {

		boolean allDone = true;
		for (ReferredRunBean referredRun : cycle.getReferredRuns()) {

			/*
			 * Goes before the `NOT_STARTED_YET` case, so it will not happen that
			 * the just started one enter this if again.
			 */
			if (referredRun.getStatus().equals(ReferredRunStatus.IN_PROGRESS)) {
				try {
					RemoteRunBean remoteRun = remoteRunRepository.getRun(referredRun.getOriginalJobId(),
							referredRun.getOriginalRunId());
					referredRun.updateFromRemoteRun(remoteRun);
				}
				catch (RunGetterException e) {
					/*
					 * This happens if the run (since it is a different microservice) has been deleted
					 * by some reason.
					 */
					referredRun.setStatus(ReferredRunStatus.ERROR);
					referredRun.setErrorMessage(e.getMessage());
				}

				if (referredRun.getStatus().equals(ReferredRunStatus.IN_PROGRESS)) {
					allDone = false;
				}
			}
			
			if (referredRun.getStatus().equals(ReferredRunStatus.NOT_STARTED_YET)) {
				
				boolean canSkip = false;
				boolean canStart = true;
				for (ReferredRunBean referredUpstreamRun : referredRun.getReferredUpstreamRuns()) {
					if (referredUpstreamRun.getStatus().equals(ReferredRunStatus.NOT_STARTED_YET)
							|| referredUpstreamRun.getStatus().equals(ReferredRunStatus.IN_PROGRESS)) {
						canStart = false;
						allDone = false;
						break;
					}
					if (referredUpstreamRun.getStatus().equals(ReferredRunStatus.ERROR)) {
						referredRun.setStatus(ReferredRunStatus.SKIP);
						canSkip = true;
						break;
					}
					if (referredUpstreamRun.getStatus().equals(ReferredRunStatus.ABORT)) {
						referredRun.setStatus(ReferredRunStatus.SKIP);
						canSkip = true;
						break;
					}
					if (referredUpstreamRun.getStatus().equals(ReferredRunStatus.SKIP)) {
						referredRun.setStatus(ReferredRunStatus.SKIP);
						canSkip = true;
						break;
					}
					if (referredUpstreamRun.getStatus().equals(ReferredRunStatus.FAIL)) {
						referredRun.setStatus(ReferredRunStatus.SKIP);
						canSkip = true;
						break;
					}
				}

				if (canSkip == false && canStart == true) {
					try {
						Map<String, String> parameterValuePair = new HashMap<String, String>();
						for (InputMapBean inputMap : referredRun.getInputMaps()) {
							parameterValuePair.put(inputMap.getRemoteName(), inputMap.getInput().getValue());
						}
						
						RemoteRunBean remoteRun = remoteRunRepository.triggerRun(referredRun.getOriginalJobId(), parameterValuePair);
						referredRun.updateFromRemoteRun(remoteRun);
						if (remoteRun.getStatus().equals("IN_PROGRESS")) {
							allDone = false;
						}
					}
					catch (RunTriggerException e) {
						referredRun.setStatus(ReferredRunStatus.ERROR);
						referredRun.setErrorMessage(e.getMessage());
					}
				}
			}

			if (referredRun.getStatus().equals(ReferredRunStatus.ERROR)) {
				cycle.setUnfinalizedStatus(CycleStatus.FAIL);
			}
			if (referredRun.getStatus().equals(ReferredRunStatus.FAIL)) {
				cycle.setUnfinalizedStatus(CycleStatus.FAIL);
			}
			if (referredRun.getStatus().equals(ReferredRunStatus.ABORT)) {
				if (!cycle.getUnfinalizedStatus().equals(CycleStatus.FAIL)) {
					cycle.setUnfinalizedStatus(CycleStatus.ABORT);
				}
			}

			/*
			 * TODO: Consider cases with multiple referredRuns but no dependency (parallel).
			 * We should finish all IN_PROGRESS ones even if other ones are failed/aborted.
			 */
		}

		if (allDone == true) {
			if (cycle.getStatus().equals(CycleStatus.IN_PROGRESS)) {
				cycle.setStatus(cycle.getUnfinalizedStatus());
			}

			cycle.setCompleteAt(new Date());
		}
		
		return cycleRepository.saveAndFlush(cycle);
	}

	@Override
	public void deleteCycle(Integer cycleId) throws IOException {

		CycleBean cycle = getCycle(cycleId);
		log.info("Delete cycle: {}", cycle);

		cycleRepository.delete(cycle);
	}
}
