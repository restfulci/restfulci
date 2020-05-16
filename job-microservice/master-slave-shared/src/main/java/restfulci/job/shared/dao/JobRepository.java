package restfulci.job.shared.dao;

import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;

import restfulci.job.shared.domain.JobBean;

public interface JobRepository extends PagingAndSortingRepository<JobBean, Integer> {

	public Optional<JobBean> findById(Integer id);
	public JobBean saveAndFlush(JobBean job);
	public void delete(JobBean job);
}
