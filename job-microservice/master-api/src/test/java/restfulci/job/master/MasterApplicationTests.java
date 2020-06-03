package restfulci.job.master;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import restfulci.job.master.api.JobsController;
import restfulci.job.master.api.RunsController;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class MasterApplicationTests {
	
	@Autowired JobsController jobsController;
	@Autowired RunsController runsController;

	@Test
	void contextLoads() {
		
		assertNotNull(jobsController);
		assertNotNull(runsController);
	}
}
