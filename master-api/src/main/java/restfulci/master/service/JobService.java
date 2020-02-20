package restfulci.master.service;

import java.io.IOException;

import restfulci.master.domain.JobBean;
import restfulci.master.dto.JobDTO;

public interface JobService {

	public JobBean getJob(Integer jobId) throws IOException;
	public JobBean createJob(JobDTO jobDTO) throws IOException;
	public void deleteJob(JobBean job);
}
