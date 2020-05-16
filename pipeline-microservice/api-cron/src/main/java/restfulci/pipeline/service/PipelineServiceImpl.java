package restfulci.pipeline.service;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import restfulci.pipeline.dao.PipelineRepository;
import restfulci.pipeline.domain.PipelineBean;
import restfulci.pipeline.domain.ReferredJobBean;

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

	@Override
	public PipelineBean addReferredJob(Integer pipelineId, ReferredJobBean referredJob) throws IOException {
		
		PipelineBean pipeline = getPipeline(pipelineId);
		log.info("Add referred job "+referredJob+" to pipeline "+pipeline);
		
		pipeline.addReferredJob(referredJob);
		referredJob.setPipeline(pipeline);
		return pipelineRepository.saveAndFlush(pipeline);
	}

	@Override
	public PipelineBean addReferredUpstreamJob(Integer pipelineId, Integer referredJobId, Integer referredUpstreamJobId) throws IOException {
		
		PipelineBean pipeline = getPipeline(pipelineId);
		
		ReferredJobBean referredJob = pipeline.getReferredJob(referredJobId);
		ReferredJobBean referredUpstreamJob = pipeline.getReferredJob(referredUpstreamJobId);
		
		if (referredJob != null && referredUpstreamJob != null) {
			/*
			 * TODO:
			 * Check if there's circular relationship, and raise exception if that's
			 * the case.
			 */
			referredJob.addReferredUpstreamJob(referredUpstreamJob);
		}
		else {
			/*
			 * So this (rather than get referredJob from some `referredJobRepository`)
			 * guarantees that both referred jobs are belonging to this pipeline. 
			 * 
			 * TODO:
			 * Raise exception if that's not the case.
			 */
		}
		
		return pipelineRepository.saveAndFlush(pipeline);
	}
}
