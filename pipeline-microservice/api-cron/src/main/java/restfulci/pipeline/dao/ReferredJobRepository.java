package restfulci.pipeline.dao;

import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;

import restfulci.pipeline.domain.ReferredJobBean;

public interface ReferredJobRepository extends PagingAndSortingRepository<ReferredJobBean, Integer> {

	public Optional<ReferredJobBean> findById(Integer id);
	public ReferredJobBean saveAndFlush(ReferredJobBean pipeline);
}
