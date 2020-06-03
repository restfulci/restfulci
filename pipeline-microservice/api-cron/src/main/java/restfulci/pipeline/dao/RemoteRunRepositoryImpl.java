package restfulci.pipeline.dao;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import restfulci.pipeline.domain.RemoteRunBean;

@Repository
public class RemoteRunRepositoryImpl implements RemoteRunRepository {
	
	@Autowired private RestTemplate restTemplate;
	
	@Override
	public RemoteRunBean triggerRun(Integer jobId) {
		
		Map<String, Object> postBody = new HashMap<String, Object>();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<Map<String, Object>> request = new HttpEntity<>(postBody, headers);
		
		return restTemplate.postForObject("/jobs/{jobId}/runs", request, RemoteRunBean.class, jobId);
	}

	@Override
	public RemoteRunBean getRun(Integer jobId, Integer runId) {
		return restTemplate.getForObject("/jobs/{jobId}/runs/{runId}", RemoteRunBean.class, jobId, runId);
	}
}
