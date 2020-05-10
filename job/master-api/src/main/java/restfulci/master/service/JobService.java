package restfulci.master.service;

import java.io.IOException;

import restfulci.master.dto.JobDTO;
import restfulci.shared.domain.JobBean;
import restfulci.shared.domain.ParameterBean;

public interface JobService {

	public JobBean getJob(Integer jobId) throws IOException;
	public JobBean createJob(JobDTO jobDTO) throws IOException;
	public void deleteJob(JobBean job);
	
	public JobBean addParameter(JobBean job, ParameterBean parameter);
}
