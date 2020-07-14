package restfulci.pipeline.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import restfulci.pipeline.domain.RemoteJobBean;

@Repository
public class RemoteJobRepositoryImpl implements RemoteJobRepository {
	
	@Autowired private RestTemplate restTemplate;

	@Override
	public RemoteJobBean getJob(Integer jobId) {
		return restTemplate.getForObject("/jobs/{jobId}", RemoteJobBean.class, jobId);
	}
}
