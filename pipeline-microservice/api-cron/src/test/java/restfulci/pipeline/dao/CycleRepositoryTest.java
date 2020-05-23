package restfulci.pipeline.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import restfulci.pipeline.domain.CycleBean;
import restfulci.pipeline.domain.CycleStatus;
import restfulci.pipeline.domain.PipelineBean;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class CycleRepositoryTest {
	
	@Autowired private PipelineRepository pipelineRepository;
	@Autowired private CycleRepository cycleRepository;

	@Test
	public void testFindInProgress() throws Exception {
		
		PipelineBean pipeline = new PipelineBean();
		pipeline.setName("unittest_pipeline");
		
		pipelineRepository.saveAndFlush(pipeline);
		
		CycleBean inProgressCycle = new CycleBean();
		inProgressCycle.setPipeline(pipeline);
		inProgressCycle.setStatus(CycleStatus.IN_PROGRESS);
		inProgressCycle.setTriggerAt(new Date());
		cycleRepository.saveAndFlush(inProgressCycle);
		
		CycleBean succeedCycle = new CycleBean();
		succeedCycle.setPipeline(pipeline);
		succeedCycle.setStatus(CycleStatus.SUCCEED);
		succeedCycle.setTriggerAt(new Date());
		cycleRepository.saveAndFlush(succeedCycle);
		
		List<CycleBean> inProgressCycles = cycleRepository.findByStatus(CycleStatus.IN_PROGRESS);
		assertEquals(inProgressCycles.size(), 1);
		assertEquals(inProgressCycles.get(0).getStatus(), CycleStatus.IN_PROGRESS);
		
		pipelineRepository.delete(pipeline);
	}
}
