package restfulci.pipeline.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import restfulci.pipeline.domain.RemoteJobBean;

@Repository
public class RemoteJobRepositoryImpl implements RemoteJobRepository {
	
	@Autowired private RestTemplate restTemplate;

	@Override
	public RemoteJobBean getJob(Integer jobId, String token) {
		
		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION, token);
		
		HttpEntity<Object> request = new HttpEntity<>(null, headers);
		
		ResponseEntity<RemoteJobBean> response = restTemplate.exchange(
				"/jobs/{jobId}", 
				HttpMethod.GET, 
				request, 
				RemoteJobBean.class, 
				jobId);
		return response.getBody();
	}
}
