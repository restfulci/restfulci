package restfulci.pipeline.dao;

import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;

import restfulci.pipeline.domain.PipelineBean;

public interface PipelineRepository extends PagingAndSortingRepository<PipelineBean, Integer> {

	public Optional<PipelineBean> findById(Integer id);
	public PipelineBean saveAndFlush(PipelineBean pipeline);
	public void delete(PipelineBean pipeline);
}
