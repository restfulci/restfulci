package restfulci.pipeline.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import restfulci.pipeline.domain.RunBean;

@Repository
public class RunRepositoryImpl implements RunRepository {
	
	@Autowired private RestTemplate restTemplate;
	
	@Override
	public RunBean triggerRun(Integer jobId) {
		return restTemplate.postForObject("/jobs/{jobId}/runs", null, RunBean.class, jobId);
	}

	@Override
	public RunBean getRun(Integer jobId, Integer runId) {
		return restTemplate.getForObject("/jobs/{jobId}/runs/{runId}", RunBean.class, jobId, runId);
	}
}
