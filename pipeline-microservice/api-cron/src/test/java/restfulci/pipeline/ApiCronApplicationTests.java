package restfulci.pipeline;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import restfulci.pipeline.api.CyclesController;
import restfulci.pipeline.api.PipelinesController;
import restfulci.pipeline.cron.UpdateInProgressCyclesCron;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class ApiCronApplicationTests {
	
	@Autowired PipelinesController pipelinesController;
	@Autowired CyclesController cyclesController;
	
	@Autowired UpdateInProgressCyclesCron updateInProgressCyclesCron;

	@Test
	void contextLoads() {
		
		assertNotNull(pipelinesController);
		assertNotNull(cyclesController);
		
		assertNotNull(updateInProgressCyclesCron);
	}
}