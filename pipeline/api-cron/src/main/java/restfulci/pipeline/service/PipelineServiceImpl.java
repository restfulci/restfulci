package restfulci.pipeline.service;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import restfulci.pipeline.dao.PipelineRepository;
import restfulci.pipeline.domain.PipelineBean;

@Service
public class PipelineServiceImpl implements PipelineService {

	@Autowired private PipelineRepository pipelineRepository;
	
	@Override
	public PipelineBean getPipeline(Integer pipelineId) throws IOException {
		
		Optional<PipelineBean> pipelines = pipelineRepository.findById(pipelineId);
		if (pipelines.isPresent()) {
			return pipelines.get();
		}
		else {
			throw new IOException();
		}
	}

	@Override
	public PipelineBean createPipeline(PipelineBean pipeline) {
		
		return pipelineRepository.saveAndFlush(pipeline);
	}

	@Override
	public void deletePipeline(PipelineBean pipeline) {
		
		pipelineRepository.delete(pipeline);
	}

}
