package restfulci.pipeline.dao;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import restfulci.pipeline.domain.RemoteRunBean;
import restfulci.pipeline.exception.RunGetterException;
import restfulci.pipeline.exception.RunTriggerException;

@Repository
public class RemoteRunRepositoryImpl implements RemoteRunRepository {
	
	@Autowired private RestTemplate restTemplate;
	
	@Override
	public RemoteRunBean triggerRun(Integer jobId, Map<String, String> parameterValuePair) throws IOException {
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<Map<String, String>> request = new HttpEntity<>(parameterValuePair, headers);
		
		try {
			return restTemplate.postForObject("/jobs/{jobId}/runs", request, RemoteRunBean.class, jobId);
		}
		catch (HttpClientErrorException e) {
			throw new RunTriggerException(e.getMessage());
		}
		catch (HttpServerErrorException e) {
			throw new RunTriggerException(e.getMessage());
		}
	}

	@Override
	public RemoteRunBean getRun(Integer jobId, Integer runId) throws IOException {
		try {
			return restTemplate.getForObject("/jobs/{jobId}/runs/{runId}", RemoteRunBean.class, jobId, runId);
		}
		catch (HttpClientErrorException e) {
			throw new RunGetterException(e.getMessage());
		}
		catch (HttpServerErrorException e) {
			throw new RunGetterException(e.getMessage());
		}
	}
}
