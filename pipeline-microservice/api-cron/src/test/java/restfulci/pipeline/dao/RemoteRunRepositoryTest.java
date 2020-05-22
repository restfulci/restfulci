package restfulci.pipeline.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import restfulci.pipeline.domain.RemoteRunBean;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@DisabledIfEnvironmentVariable(named="CI", matches="CircleCI")
public class RemoteRunRepositoryTest {
	
	@Autowired private RemoteRunRepository runRepository;

	@Test
	public void testTriggerRun() throws Exception {
		
		RemoteRunBean run = runRepository.triggerRun(1);
		assertEquals(run.getId(), 1);
		assertEquals(run.getStatus(), "IN_PROGRESS");
		assertNull(run.getExitCode());
	}
	
	@Test
	public void testGetRun() throws Exception {
		
		RemoteRunBean run = runRepository.getRun(1, 1);
		assertEquals(run.getId(), 1);
		assertEquals(run.getStatus(), "IN_PROGRESS");
		assertNull(run.getExitCode());
	}
}
