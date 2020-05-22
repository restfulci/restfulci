package restfulci.pipeline.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import restfulci.pipeline.domain.RemoteRunBean;

@Repository
public class RemoteRunRepositoryImpl implements RemoteRunRepository {
	
	@Autowired private RestTemplate restTemplate;
	
	@Override
	public RemoteRunBean triggerRun(Integer jobId) {
		return restTemplate.postForObject("/jobs/{jobId}/runs", null, RemoteRunBean.class, jobId);
	}

	@Override
	public RemoteRunBean getRun(Integer jobId, Integer runId) {
		return restTemplate.getForObject("/jobs/{jobId}/runs/{runId}", RemoteRunBean.class, jobId, runId);
	}
}
