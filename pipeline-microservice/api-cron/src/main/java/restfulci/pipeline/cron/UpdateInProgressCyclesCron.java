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
			 * 
			 * TODO:
			 * Save token to a temporary storage, and retrieve it in here.
			 * Looks like passing JWT is a common practice here, then we just
			 * need to store it.
			 * https://medium.facilelogin.com/securing-microservices-with-oauth-2-0-jwt-and-xacml-d03770a9a838#d912
			 * 
			 * TODO:
			 * There's race condition here. Once we are ready to give another call
			 * to the downstream job-microservice, the JWT may already expired. 
			 * Pipeline doesn't have refresh token so cannot renew it either.
			 */
			log.info("Update cycle: {}", cycle);
			cycleService.updateCycle(cycle, "mocked_token");
		}
	}
}
