package restfulci.master.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import restfulci.master.domain.JobBean;

@Repository
public class JobRepositoryImpl implements JobRepository {

	@Autowired private JobDatabaseRepository jobDatabaseRepository;
	
	@Override
	public Optional<JobBean> findById(Integer id) {
		Optional<JobBean> job = jobDatabaseRepository.findById(id);
		return job;
	}

	@Override
	public JobBean saveAndFlush(JobBean job) {
		return jobDatabaseRepository.saveAndFlush(job);
	}

	@Override
	public void delete(JobBean job) {
		jobDatabaseRepository.delete(job);
	}

}
