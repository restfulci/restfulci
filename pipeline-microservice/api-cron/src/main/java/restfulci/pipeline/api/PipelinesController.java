package restfulci.pipeline.api;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import restfulci.pipeline.domain.ParameterBean;
import restfulci.pipeline.domain.PipelineBean;
import restfulci.pipeline.domain.ReferredJobBean;
import restfulci.pipeline.service.PipelineService;

@RestController
@RequestMapping(value="/pipelines")
public class PipelinesController {

	@Autowired private PipelineService pipelineService;
	
	@PostMapping
	public PipelineBean createPipeline(@RequestBody @Valid PipelineBean pipeline) throws Exception {
		
		return pipelineService.createPipeline(pipeline);
	}
	
	@GetMapping("/{pipelineId}")
	public PipelineBean getPipeline(@PathVariable @Min(1) Integer pipelineId) throws Exception {
		
		return pipelineService.getPipeline(pipelineId);
	}
	
	@DeleteMapping("/{pipelineId}")
	public void deletePipeline(@PathVariable @Min(1) Integer pipelineId) throws Exception {
		
		pipelineService.deletePipeline(pipelineId);
	}
	
	@PostMapping("/{pipelineId}/parameters")
	public PipelineBean addParameter(
			@PathVariable @Min(1) Integer pipelineId,
			@RequestBody @Valid ParameterBean parameter) throws Exception {
		
		return pipelineService.addParameter(pipelineId, parameter);
	}
	
	@PostMapping("/{pipelineId}/referred-jobs")
	public PipelineBean addReferredJob(
			@PathVariable @Min(1) Integer pipelineId,
			@RequestBody @Valid ReferredJobBean referredJob) throws Exception {
		
		return pipelineService.addReferredJob(pipelineId, referredJob);
	}
	
	@PutMapping("/{pipelineId}/referred-jobs/{referredJobId}")
	public ReferredJobBean updateReferredJobParameters(
			@PathVariable @Min(1) Integer pipelineId,
			@PathVariable @Min(1) Integer referredJobId) throws Exception {
		
		return pipelineService.updateReferredJobParameters(referredJobId);
	}
	
	@PutMapping("/{pipelineId}/referred-jobs/{referredJobId}/parameter-maps/{parameterMapId}")
	public PipelineBean linkReferredJobParameter(
			@PathVariable @Min(1) Integer pipelineId,
			@PathVariable @Min(1) Integer referredJobId,
			@PathVariable @Min(1) Integer parameterMapId,
			@RequestParam @Min(1) Integer parameterId) throws Exception {
		
		return pipelineService.linkReferredJobParameter(pipelineId, referredJobId, parameterMapId, parameterId);
	}
	
	@PutMapping("/{pipelineId}/referred-jobs/{referredJobId}/referred-upstream-jobs/{referredUpstreamJobId}")
	public PipelineBean addReferredUpstreamJob(
			@PathVariable @Min(1) Integer pipelineId,
			@PathVariable @Min(1) Integer referredJobId,
			@PathVariable @Min(1) Integer referredUpstreamJobId) throws Exception {
		
		return pipelineService.addReferredUpstreamJob(pipelineId, referredJobId, referredUpstreamJobId);
	}
}
