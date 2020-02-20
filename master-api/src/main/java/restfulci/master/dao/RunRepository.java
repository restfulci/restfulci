package restfulci.master.dao;

import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;

import restfulci.master.domain.RunBean;

public interface RunRepository extends PagingAndSortingRepository<RunBean, Integer> {

	public Optional<RunBean> findById(Integer id);
	public RunBean saveAndFlush(RunBean run);
	public void delete(RunBean run);
}
