package restfulci.pipeline.dao;

import java.io.IOException;
import java.util.Map;

import restfulci.pipeline.domain.RemoteRunBean;

public interface RemoteRunRepository {

	public RemoteRunBean triggerRun(
			Integer jobId, 
			Map<String, String> parameterValuePair,
			String token) throws IOException;
	public RemoteRunBean getRun(
			Integer jobId, 
			Integer runId,
			String token) throws IOException;
}
