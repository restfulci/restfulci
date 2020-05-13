package restfulci.pipeline.service;

import java.io.IOException;

import restfulci.pipeline.domain.PipelineBean;

public interface PipelineService {

	public PipelineBean getPipeline(Integer pipelineId) throws IOException;
	public PipelineBean createPipeline(PipelineBean pipeline);
	public void deletePipeline(Integer pipelineId) throws IOException;
}
