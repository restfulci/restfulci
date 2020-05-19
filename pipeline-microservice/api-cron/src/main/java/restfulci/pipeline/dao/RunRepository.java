package restfulci.pipeline.dao;

import restfulci.pipeline.domain.RunBean;

public interface RunRepository {

	public RunBean triggerRun(Integer jobId);
	public RunBean getRun(Integer jobId, Integer runId);
}
