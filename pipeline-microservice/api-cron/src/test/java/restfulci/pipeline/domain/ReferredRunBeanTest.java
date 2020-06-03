package restfulci.pipeline.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ReferredRunBeanTest {

	@Test
	public void testUpdateFromRemoteRun() throws Exception {
		
		ReferredRunBean referredRun = new ReferredRunBean();
		referredRun.setStatus(ReferredRunStatus.NOT_STARTED_YET);
		
		RemoteRunBean remoteRun = new RemoteRunBean();
		remoteRun.setId(123);
		remoteRun.setStatus("SUCCEED");
		remoteRun.setExitCode(0);
		
		referredRun.updateFromRemoteRun(remoteRun);
		assertEquals(referredRun.getOriginalRunId(), 123);
		assertEquals(referredRun.getStatus(), ReferredRunStatus.SUCCEED);
		assertEquals(referredRun.getExitCode(), 0);
	}
}
