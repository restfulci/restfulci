package restfulci.job.shared.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import restfulci.job.shared.domain.JobBean;
import restfulci.job.shared.domain.RunBean;

public interface RunRepository extends PagingAndSortingRepository<RunBean, Integer> {
	
	public List<RunBean> findAllByJob(JobBean job, Pageable pageable);

	public Optional<RunBean> findById(Integer id);
	public RunBean saveAndFlush(RunBean run);
	public void delete(RunBean run);
}
