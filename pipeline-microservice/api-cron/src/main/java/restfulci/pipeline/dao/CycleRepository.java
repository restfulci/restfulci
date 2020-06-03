package restfulci.pipeline.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;

import restfulci.pipeline.domain.CycleBean;
import restfulci.pipeline.domain.CycleStatus;

public interface CycleRepository extends PagingAndSortingRepository<CycleBean, Integer> {

	public Optional<CycleBean> findById(Integer id);
	public List<CycleBean> findByStatus(CycleStatus status);
	
	public CycleBean saveAndFlush(CycleBean cycle);
	public void delete(CycleBean cycle);
}
