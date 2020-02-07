package restfulci.master.dao;

import java.util.Optional;

import restfulci.master.domain.JobBean;

public interface JobRepository {

	public Optional<JobBean> findById(Integer id);
	public JobBean saveAndFlush(JobBean job);
	public void delete(JobBean job);
}
