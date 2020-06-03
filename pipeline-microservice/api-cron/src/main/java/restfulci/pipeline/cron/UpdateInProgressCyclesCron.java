package restfulci.pipeline.cron;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import restfulci.pipeline.dao.CycleRepository;
import restfulci.pipeline.domain.CycleBean;
import restfulci.pipeline.domain.CycleStatus;
import restfulci.pipeline.service.CycleService;

@Component
@Transactional
@Slf4j
public class UpdateInProgressCyclesCron {
	
	@Autowired CycleRepository cycleRepository;
	@Autowired CycleService cycleService;

	@Scheduled(fixedRate=1000)
	public void updateInProgressCycles() throws IOException {
		
		List<CycleBean> inProgressCycles = cycleRepository.findByStatus(CycleStatus.IN_PROGRESS);
		for (CycleBean cycle : inProgressCycles) {
			
			/*
			 * TODO:
			 * For each particular cycle, this should error out after a certain amount of tries.
			 * This is especially needed if something goes wrong and job microservice is keeping
			 * giving non-200 (so the referred jobs will never be in status DONE).
			 */
			log.info("Update cycle: {}", cycle);
			cycleService.updateCycle(cycle);
		}
	}
}
