package restfulci.job.master.service;

import java.io.IOException;
import java.util.List;

import restfulci.job.master.dto.JobDTO;
import restfulci.job.shared.domain.JobBean;
import restfulci.job.shared.domain.ParameterBean;

public interface JobService {
	
	public List<JobBean> listJobs(Integer page, Integer size);

	public JobBean getJob(Integer jobId) throws IOException;
	public JobBean createJob(JobDTO jobDTO) throws IOException;
	public void deleteJob(Integer jobId) throws IOException;
	
	public JobBean addParameter(Integer jobId, ParameterBean parameter) throws IOException;
	/*
	 * TODO:
	 * removeParameter
	 */
}
