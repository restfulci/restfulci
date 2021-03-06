package restfulci.pipeline.service;

import java.io.IOException;

import restfulci.pipeline.domain.ParameterBean;
import restfulci.pipeline.domain.PipelineBean;
import restfulci.pipeline.domain.ReferredJobBean;

public interface PipelineService {

	public PipelineBean getPipeline(Integer pipelineId) throws IOException;
	public PipelineBean createPipeline(PipelineBean pipeline);
	public void deletePipeline(Integer pipelineId) throws IOException;
	
	public PipelineBean addParameter(Integer pipelineId, ParameterBean parameter) throws IOException;
	public PipelineBean addReferredJob(Integer pipelineId, ReferredJobBean referredJob) throws IOException;
	/*
	 * TODO:
	 * removeReferredJob
	 */
	
	public ReferredJobBean updateReferredJobParameters(Integer referredJobId) throws IOException;
	public PipelineBean linkReferredJobParameter(
			Integer pipelineId, Integer referredJobId, Integer parameterMapId, Integer parameterId) throws IOException;
	
	public PipelineBean addReferredUpstreamJob(Integer pipelineId, Integer referredJobId, Integer referredUpstreamJobId) throws IOException;
}
