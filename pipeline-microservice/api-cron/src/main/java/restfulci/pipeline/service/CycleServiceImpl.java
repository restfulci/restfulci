package restfulci.pipeline.service;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import restfulci.pipeline.dao.CycleRepository;
import restfulci.pipeline.domain.CycleBean;
import restfulci.pipeline.domain.PipelineBean;
import restfulci.pipeline.domain.ReferredJobBean;
import restfulci.pipeline.domain.ReferredRunBean;
import restfulci.pipeline.domain.ReferredRunPhase;

@Service
@Transactional
@Slf4j
public class CycleServiceImpl implements CycleService {

	@Autowired private CycleRepository cycleRepository;
	
	@Autowired private PipelineService pipelineService;

	@Override
	public CycleBean getCycle(Integer cycleId) throws IOException {
		
		Optional<CycleBean> cycles = cycleRepository.findById(cycleId);
		if (cycles.isPresent()) {
			return cycles.get();
		}
		else {
			throw new IOException();
		}
	}

	@Override
	public CycleBean triggerCycle(Integer pipelineId) throws IOException {
		
		PipelineBean pipeline = pipelineService.getPipeline(pipelineId); 
		log.info("Create cycle under pipeline "+pipeline);
		
		CycleBean cycle = new CycleBean();
		cycle.setPipeline(pipeline);
		cycle.setTriggerAt(new Date());
		
		for (ReferredJobBean referredJob : pipeline.getReferredJobs()) {
			ReferredRunBean referredRun = new ReferredRunBean();
			referredRun.setOriginalJobId(referredJob.getOriginalJobId());
			referredRun.setPhase(ReferredRunPhase.NOT_STARTED_YET);
			
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

	@Override
	public void deleteCycle(Integer cycleId) throws IOException {
		
		CycleBean cycle = getCycle(cycleId);
		log.info("Delete cycle: "+cycle);
		
		cycleRepository.delete(cycle);
	}
}
