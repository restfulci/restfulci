package restfulci.pipeline.dao;

import restfulci.pipeline.domain.RemoteRunBean;

public interface RemoteRunRepository {

	public RemoteRunBean triggerRun(Integer jobId);
	public RemoteRunBean getRun(Integer jobId, Integer runId);
}
