package restfulci.pipeline.dao;

import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;

import restfulci.pipeline.domain.CycleBean;
import restfulci.pipeline.domain.PipelineBean;

public interface CycleRepository extends PagingAndSortingRepository<CycleBean, Integer> {

	public Optional<CycleBean> findById(Integer id);
	public CycleBean saveAndFlush(CycleBean cycle);
	public void delete(CycleBean cycle);
}
