package restfulci.pipeline.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import restfulci.pipeline.domain.RunBean;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@DisabledIfEnvironmentVariable(named="CI", matches="CircleCI")
public class RunRepositoryTest {
	
	@Autowired private RunRepository runRepository;

	@Test
	public void testTriggerRun() throws Exception {
		
		RunBean run = runRepository.triggerRun(1);
		assertEquals(run.getId(), 1);
		assertEquals(run.getPhase(), "IN_PROGRESS");
		assertNull(run.getExitCode());
	}
	
	@Test
	public void testGetRun() throws Exception {
		
		RunBean run = runRepository.getRun(1, 1);
		assertEquals(run.getId(), 1);
		assertEquals(run.getPhase(), "IN_PROGRESS");
		assertNull(run.getExitCode());
	}
}
