package restfulci.job.master.service;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import restfulci.job.master.dto.JobDTO;
import restfulci.job.shared.dao.JobRepository;
import restfulci.job.shared.domain.JobBean;
import restfulci.job.shared.domain.ParameterBean;

@Service
@Transactional
@Slf4j
public class JobServiceImpl implements JobService {
	
	@Autowired private JobRepository jobRepository;

	@Override
	public JobBean getJob(Integer jobId) throws IOException {
		
		Optional<JobBean> jobs = jobRepository.findById(jobId);
		if (jobs.isPresent()) {
			return jobs.get();
		}
		else {
			throw new IOException();
		}
	}

	@Override
	public JobBean createJob(JobDTO jobDTO) throws IOException {
		
		log.info("Create job: "+jobDTO);
		
		JobBean job = jobDTO.toBean();
		return jobRepository.saveAndFlush(job);
	}

	@Override
	public void deleteJob(Integer jobId) throws IOException {
		
		JobBean job = getJob(jobId);
		log.info("Delete job: "+job);
		
		jobRepository.delete(job);
	}

	@Override
	public JobBean addParameter(Integer jobId, ParameterBean parameter) throws IOException {
		
		JobBean job = getJob(jobId);
		log.info("Add parameter "+parameter+" to job "+job);
		
		job.addParameter(parameter);
		parameter.setJob(job);
		return jobRepository.saveAndFlush(job);
	}

}
