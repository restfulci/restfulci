package restfulci.master.service;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import restfulci.master.dto.JobDTO;
import restfulci.shared.dao.JobRepository;
import restfulci.shared.domain.JobBean;
import restfulci.shared.domain.ParameterBean;

@Service
public class JobServiceImpl implements JobService {
	
	@Autowired private JobRepository jobRepository;

	@Override
	public JobBean getJob(Integer jobId) throws IOException {
		
		Optional<JobBean> jobs = jobRepository.findById(jobId);
		if (jobs.isPresent()) {
			JobBean job = jobs.get();
			/*
			 * Load parameters since `FetchType.LAZY`.
			 */
			job.getParameters().size();
			return job;
		}
		else {
			throw new IOException();
		}
	}

	@Override
	public JobBean createJob(JobDTO jobDTO) throws IOException {
		
		JobBean job = jobDTO.toBean();
		jobRepository.saveAndFlush(job);
		return job;
	}

	@Override
	public void deleteJob(JobBean job) {
		
		jobRepository.delete(job);
	}

	@Override
	public JobBean addParameter(JobBean job, ParameterBean parameter) {
		
		job.addParameter(parameter);
		parameter.setJob(job);
		jobRepository.saveAndFlush(job);
		return job;
	}

}
