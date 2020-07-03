package restfulci.pipeline.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import restfulci.pipeline.domain.RemoteRunBean;
import restfulci.pipeline.exception.RunTriggerException;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@DisabledIfEnvironmentVariable(named="CI", matches="CircleCI")
public class RemoteRunRepositoryTest {
	
	@Autowired private RemoteRunRepository remoteRunRepository;

	@Test
	public void testTriggerAndGetRun() throws Exception {
		
		RemoteRunBean triggeredRun = remoteRunRepository.triggerRun(1, new HashMap<String, String>());
		Integer runId = triggeredRun.getId();
		assertEquals(triggeredRun.getStatus(), "IN_PROGRESS");
		assertNull(triggeredRun.getExitCode());
		
		RemoteRunBean queriedRun = remoteRunRepository.getRun(1, runId);
		assertEquals(queriedRun.getId(), runId);
		assertEquals(queriedRun.getStatus(), "IN_PROGRESS");
		assertNull(queriedRun.getExitCode());
	}
	
	@Test
	public void testTriggerRunBadRequest() throws Exception {
		
		Assertions.assertThrows(RunTriggerException.class, () -> {
			remoteRunRepository.triggerRun(31, new HashMap<String, String>());
		});
	}
	
	@Test
	public void testTriggerRunInternalServerError() throws Exception {
		
		Assertions.assertThrows(RunTriggerException.class, () -> {
			remoteRunRepository.triggerRun(41, new HashMap<String, String>());
		});
	}
}
