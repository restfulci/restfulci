package restfulci.pipeline.dao;

import restfulci.pipeline.domain.RemoteJobBean;

public interface RemoteJobRepository {

	public RemoteJobBean getJob(Integer jobId);
}
