package restfulci.pipeline.dao;

import java.io.IOException;

import restfulci.pipeline.domain.RemoteRunBean;

public interface RemoteRunRepository {

	public RemoteRunBean triggerRun(Integer jobId) throws IOException;
	public RemoteRunBean getRun(Integer jobId, Integer runId) throws IOException;
}
