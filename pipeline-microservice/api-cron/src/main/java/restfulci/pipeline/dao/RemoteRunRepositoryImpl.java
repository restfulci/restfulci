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
		/*
		 * TODO:
		 * 
		 * Currently error out because this post call needs a JSON body (which may be empty
		 * but is required). 
		 * > $ curl -X POST -H "Content-Type: application/json" --data '{}' $JOB_HOST/jobs/9/runs
		 * > {"id":4,"job":{"id":9,"name":"manual_freestyle_job_name","dockerImage":"busybox:1.31","command":["sh","-c","echo \"Hello world\""],"type":"FREESTYLE"},"status":"IN_PROGRESS","triggerAt":"2020-06-01 04:34:52","completeAt":null,"exitCode":null,"runResults":[],"type":"FREESTYLE"}%  
		 * > $ curl -X POST -H "Content-Type: application/json" $JOB_HOST/jobs/9/runs
		 * > {"timestamp":"2020-06-01T04:34:58.396+0000","status":400,"error":"Bad Request","message":"Required request body is missing: public restfulci.job.shared.domain.RunBean restfulci.job.master.api.RunsController.triggerRun(java.lang.Integer,restfulci.job.master.dto.RunDTO) throws java.lang.Exception","path":"/jobs/9/runs"}%
		 * 
		 * Also, for git run we need to pass in the SHA through here.
		 * 
		 * TODO:
		 * Kubernetes raises an different error through.
org.springframework.web.client.HttpClientErrorException$UnsupportedMediaType: 415 : [{"timestamp":"2020-06-01T04:30:22.320+0000","status":415,"error":"Unsupported Media Type","message":"Content type 'application/x-www-form-urlencoded;charset=UTF-8' not supported","path":"/jobs/2/runs"}]
	at org.springframework.web.client.HttpClientErrorException.create(HttpClientErrorException.java:133) ~[spring-web-5.2.3.RELEASE.jar:5.2.3.RELEASE]
	at org.springframework.web.client.DefaultResponseErrorHandler.handleError(DefaultResponseErrorHandler.java:170) ~[spring-web-5.2.3.RELEASE.jar:5.2.3.RELEASE]
	at org.springframework.web.client.DefaultResponseErrorHandler.handleError(DefaultResponseErrorHandler.java:112) ~[spring-web-5.2.3.RELEASE.jar:5.2.3.RELEASE]
	at org.springframework.web.client.ResponseErrorHandler.handleError(ResponseErrorHandler.java:63) ~[spring-web-5.2.3.RELEASE.jar:5.2.3.RELEASE]
	at org.springframework.web.client.RestTemplate.handleResponse(RestTemplate.java:785) ~[spring-web-5.2.3.RELEASE.jar:5.2.3.RELEASE]
	at org.springframework.web.client.RestTemplate.doExecute(RestTemplate.java:743) ~[spring-web-5.2.3.RELEASE.jar:5.2.3.RELEASE]
	at org.springframework.web.client.RestTemplate.execute(RestTemplate.java:677) ~[spring-web-5.2.3.RELEASE.jar:5.2.3.RELEASE]
	at org.springframework.web.client.RestTemplate.postForObject(RestTemplate.java:421) ~[spring-web-5.2.3.RELEASE.jar:5.2.3.RELEASE]
	at restfulci.pipeline.dao.RemoteRunRepositoryImpl.triggerRun(RemoteRunRepositoryImpl.java:16) ~[classes/:na]
		 */
	}

	@Override
	public RemoteRunBean getRun(Integer jobId, Integer runId) {
		return restTemplate.getForObject("/jobs/{jobId}/runs/{runId}", RemoteRunBean.class, jobId, runId);
	}
}
