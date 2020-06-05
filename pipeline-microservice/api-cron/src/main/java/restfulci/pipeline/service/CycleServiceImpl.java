package restfulci.pipeline.service;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import restfulci.pipeline.dao.CycleRepository;
import restfulci.pipeline.dao.RemoteRunRepository;
import restfulci.pipeline.domain.CycleBean;
import restfulci.pipeline.domain.CycleStatus;
import restfulci.pipeline.domain.PipelineBean;
import restfulci.pipeline.domain.ReferredJobBean;
import restfulci.pipeline.domain.ReferredRunBean;
import restfulci.pipeline.domain.ReferredRunStatus;
import restfulci.pipeline.domain.RemoteRunBean;
import restfulci.pipeline.exception.IdNonExistenceException;

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
	public CycleBean triggerCycle(Integer pipelineId) throws IOException {

		PipelineBean pipeline = pipelineService.getPipeline(pipelineId);
		log.info("Create cycle under pipeline {}", pipeline);

		CycleBean cycle = new CycleBean();
		cycle.setPipeline(pipeline);
		cycle.setStatus(CycleStatus.IN_PROGRESS);
		cycle.setTriggerAt(new Date());

		for (ReferredJobBean referredJob : pipeline.getReferredJobs()) {
			ReferredRunBean referredRun = new ReferredRunBean();
			referredRun.setOriginalJobId(referredJob.getOriginalJobId());
			referredRun.setStatus(ReferredRunStatus.NOT_STARTED_YET);

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
	public void updateCycle(CycleBean cycle) throws IOException {

		boolean allDone = true;
		for (ReferredRunBean referredRun : cycle.getReferredRuns()) {

			/*
			 * Goes before the `NOT_STARTED_YET` case, so it will not happen that
			 * the just started one enter this if again.
			 */
			if (referredRun.getStatus().equals(ReferredRunStatus.IN_PROGRESS)) {
				RemoteRunBean remoteRun = remoteRunRepository.getRun(referredRun.getOriginalJobId(),
						referredRun.getOriginalRunId());
				referredRun.updateFromRemoteRun(remoteRun);

				if (referredRun.getStatus().equals(ReferredRunStatus.IN_PROGRESS)) {
					allDone = false;
				}
			}
			
			if (referredRun.getStatus().equals(ReferredRunStatus.NOT_STARTED_YET)) {
				allDone = false;

				boolean canStart = true;
				for (ReferredRunBean referredUpstreamRun : referredRun.getReferredUpstreamRuns()) {
					if (referredUpstreamRun.getStatus().equals(ReferredRunStatus.NOT_STARTED_YET)
							|| referredUpstreamRun.getStatus().equals(ReferredRunStatus.IN_PROGRESS)) {
						canStart = false;
						break;
					}
					if (referredUpstreamRun.getStatus().equals(ReferredRunStatus.ABORT)) {
						referredRun.setStatus(ReferredRunStatus.SKIP);
						canStart = false;
						break;
					}
					if (referredUpstreamRun.getStatus().equals(ReferredRunStatus.SKIP)) {
						referredRun.setStatus(ReferredRunStatus.SKIP);
						canStart = false;
						break;
					}
					if (referredUpstreamRun.getStatus().equals(ReferredRunStatus.FAIL)) {
						referredRun.setStatus(ReferredRunStatus.SKIP);
						canStart = false;
						break;
					}
				}

				if (canStart == true) {
					RemoteRunBean remoteRun = remoteRunRepository.triggerRun(referredRun.getOriginalJobId());
					referredRun.updateFromRemoteRun(remoteRun);
				}
			}

			if (referredRun.getStatus().equals(ReferredRunStatus.FAIL)) {
				cycle.setStatus(CycleStatus.FAIL);
			}
			if (cycle.getStatus() == null && referredRun.getStatus().equals(ReferredRunStatus.ABORT)) {
				cycle.setStatus(CycleStatus.ABORT);
			}

			/*
			 * TODO: Consider cases with multiple referredRuns but no dependency (parallel).
			 * We should finish all IN_PROGRESS ones even if other ones are failed/aborted.
			 */
		}

		if (allDone == true) {
			if (cycle.getStatus().equals(CycleStatus.IN_PROGRESS)) {
				cycle.setStatus(CycleStatus.SUCCEED);
			}

			cycle.setCompleteAt(new Date());
		}
	}

	@Override
	public void deleteCycle(Integer cycleId) throws IOException {

		CycleBean cycle = getCycle(cycleId);
		log.info("Delete cycle: {}", cycle);

		cycleRepository.delete(cycle);
	}
}
