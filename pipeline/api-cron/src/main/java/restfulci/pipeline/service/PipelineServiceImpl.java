package restfulci.pipeline.service;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import restfulci.pipeline.api.PipelinesController;
import restfulci.pipeline.dao.PipelineRepository;
import restfulci.pipeline.domain.PipelineBean;

@Service
@Transactional
@Slf4j
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
		
		log.info("Create pipeline: "+pipeline);
		return pipelineRepository.saveAndFlush(pipeline);
	}

	@Override
	public void deletePipeline(Integer pipelineId) throws IOException {
		
		PipelineBean pipeline = getPipeline(pipelineId);
		log.info("Delete pipeline: "+pipeline);
		
		pipelineRepository.delete(pipeline);
	}

}
