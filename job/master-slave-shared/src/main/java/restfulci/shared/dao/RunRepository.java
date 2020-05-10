package restfulci.shared.dao;

import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;

import restfulci.shared.domain.RunBean;

public interface RunRepository extends PagingAndSortingRepository<RunBean, Integer> {

	public Optional<RunBean> findById(Integer id);
	public RunBean saveAndFlush(RunBean run);
	public void delete(RunBean run);
}
