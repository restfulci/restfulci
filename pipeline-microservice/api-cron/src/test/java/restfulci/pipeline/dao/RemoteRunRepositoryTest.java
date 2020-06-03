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
	public void testTriggerAndGetRun() throws Exception {
		
		RemoteRunBean triggeredRun = runRepository.triggerRun(1);
		Integer runId = triggeredRun.getId();
		assertEquals(triggeredRun.getStatus(), "IN_PROGRESS");
		assertNull(triggeredRun.getExitCode());
		
		RemoteRunBean queriedRun = runRepository.getRun(1, runId);
		assertEquals(queriedRun.getId(), runId);
		assertEquals(queriedRun.getStatus(), "IN_PROGRESS");
		assertNull(queriedRun.getExitCode());
	}
}