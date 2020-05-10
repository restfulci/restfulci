package restfulci.shared.dao;	

import java.util.Optional;	

import org.springframework.data.repository.PagingAndSortingRepository;	

import restfulci.shared.domain.RunResultBean;	

public interface RunResultRepository extends PagingAndSortingRepository<RunResultBean, Integer> {	

	public Optional<RunResultBean> findById(Integer id);	
	
	/*
	 * Always `saveAndFlush()` from `RunRepository`.
	 */
}