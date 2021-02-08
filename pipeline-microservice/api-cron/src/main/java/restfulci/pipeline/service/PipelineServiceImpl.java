package restfulci.pipeline.service;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import restfulci.pipeline.dao.PipelineRepository;
import restfulci.pipeline.dao.ReferredJobRepository;
import restfulci.pipeline.dao.RemoteJobRepository;
import restfulci.pipeline.domain.ParameterBean;
import restfulci.pipeline.domain.ParameterMapBean;
import restfulci.pipeline.domain.PipelineBean;
import restfulci.pipeline.domain.ReferredJobBean;
import restfulci.pipeline.domain.RemoteJobBean;
import restfulci.pipeline.exception.IdNonExistenceException;

@Service
@Transactional
@Slf4j
public class PipelineServiceImpl implements PipelineService {

	@Autowired private PipelineRepository pipelineRepository;
	@Autowired private ReferredJobRepository referredJobRepository;
	@Autowired private RemoteJobRepository remoteJobRepository;
	
	@Override
	public PipelineBean getPipeline(Integer pipelineId) throws IOException {
		
		Optional<PipelineBean> pipelines = pipelineRepository.findById(pipelineId);
		if (pipelines.isPresent()) {
			return pipelines.get();
		}
		else {
			throw new IdNonExistenceException("Pipeline ID does not exist yet.");
		}
	}
	
	private ReferredJobBean getReferredJob(Integer referredJobId) throws IOException {
		
		Optional<ReferredJobBean> ReferredJobs = referredJobRepository.findById(referredJobId);
		if (ReferredJobs.isPresent()) {
			return ReferredJobs.get();
		}
		else {
			throw new IdNonExistenceException("Referred job ID does not exist yet.");
		}
	}

	@Override
	public PipelineBean createPipeline(PipelineBean pipeline) {
		
		log.info("Create pipeline: {}", pipeline);
		return pipelineRepository.saveAndFlush(pipeline);
	}

	@Override
	public void deletePipeline(Integer pipelineId) throws IOException {
		
		PipelineBean pipeline = getPipeline(pipelineId);
		log.info("Delete pipeline: {}", pipeline);
		
		pipelineRepository.delete(pipeline);
	}
	
	@Override
	public PipelineBean addParameter(Integer pipelineId, ParameterBean parameter) throws IOException {
		
		PipelineBean pipeline = getPipeline(pipelineId);
		log.info("Add parameter {} to pipeline {}", parameter, pipeline);
		
		pipeline.addParameter(parameter);
		parameter.setPipeline(pipeline);
		return pipelineRepository.saveAndFlush(pipeline);
	}

	@Override
	public PipelineBean addReferredJob(Integer pipelineId, ReferredJobBean referredJob) throws IOException {
		
		PipelineBean pipeline = getPipeline(pipelineId);
		log.info("Add referred job {} to pipeline {}", referredJob, pipeline);
		
		pipeline.addReferredJob(referredJob);
		referredJob.setPipeline(pipeline);
		return pipelineRepository.saveAndFlush(pipeline);
	}
	
	@Override
	public ReferredJobBean updateReferredJobParameters(
			Integer referredJobId, 
			String token) throws IOException {
		
		ReferredJobBean referredJob = getReferredJob(referredJobId);
		
		RemoteJobBean remoteJob = remoteJobRepository.getJob(referredJob.getOriginalJobId(), token);
		for (RemoteJobBean.Parameter remoteParameter : remoteJob.getParameters()) {
			ParameterMapBean parameterMap = referredJob.getParameterMap(remoteParameter.getName());
			if (parameterMap == null) {
				ParameterMapBean newParameterMap = new ParameterMapBean();
				newParameterMap.setRemoteName(remoteParameter.getName());
				newParameterMap.setOptional(remoteParameter.isOptional());
				
				newParameterMap.setReferredJob(referredJob);
				referredJob.addParameterMap(newParameterMap);
			}
			else {
				parameterMap.setOptional(remoteParameter.isOptional());
			}
		}
		
		if (remoteJob.getType().equals("GIT")) {
			for (String remoteName : new String[]{"branchName", "commitSha"}) {
				if (referredJob.getParameterMap(remoteName) == null) {
					ParameterMapBean newParameterMap = new ParameterMapBean();
					newParameterMap.setRemoteName(remoteName);
					newParameterMap.setOptional(true);
					
					newParameterMap.setReferredJob(referredJob);
					referredJob.addParameterMap(newParameterMap);
				}
			}
		}
		
		for (ParameterMapBean parameterMap : referredJob.getParameterMaps()) {
			String remoteName = parameterMap.getRemoteName();
			
			if (remoteName.equals("branchName") || remoteName.equals("commitSha")) {
				continue;
			}
			
			if (remoteJob.getParameter(parameterMap.getRemoteName()) == null) {
				referredJob.removeParameterMap(parameterMap);
			}
		}
			
		/*
		 * TODO:
		 * We need to build an "or" relationship for `parameter` and
		 * `parameter_map`, to handle git remote job, for which we need
		 * to pass either `branchName` or `commitSha`.
		 */
		
		return referredJobRepository.saveAndFlush(referredJob);
	}
	
	@Override
	public PipelineBean linkReferredJobParameter(
			Integer pipelineId, Integer referredJobId, Integer parameterMapId, Integer parameterId) throws IOException {
	
		PipelineBean pipeline = getPipeline(pipelineId);
		ParameterBean parameter = pipeline.getParameter(parameterId);
		/*
		 * TODO:
		 * Raise 400 if parameter is null. Give message that `parameterId` does not belongs 
		 * to pipeline with `pipelineId`.
		 */
		
		ReferredJobBean referredJob = pipeline.getReferredJob(referredJobId);
		ParameterMapBean parameterMap = referredJob.getParameterMap(parameterMapId);
		parameterMap.setParameter(parameter);
		/*
		 * TODO:
		 * Raise 404 if referredJob or parameterMap is null.
		 */
		
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
