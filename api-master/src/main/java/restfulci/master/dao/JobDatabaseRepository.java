package restfulci.master.dao;

import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;

import restfulci.master.domain.JobBean;

interface JobDatabaseRepository extends PagingAndSortingRepository<JobBean, Integer> {

	Optional<JobBean> findById(Integer id);
	JobBean saveAndFlush(JobBean job);
	void delete(JobBean job);
}