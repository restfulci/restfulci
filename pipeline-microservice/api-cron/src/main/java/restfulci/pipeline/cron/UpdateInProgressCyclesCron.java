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
			
			log.info("Update cycle: {}", cycle);
			cycleService.updateCycle(cycle);
		}
	}
}
