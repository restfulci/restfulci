package restfulci.pipeline.api;

import java.io.IOException;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import restfulci.pipeline.domain.PipelineBean;
import restfulci.pipeline.service.PipelineService;

@RestController
@RequestMapping(value="/pipelines")
public class PipelinesController {

	@Autowired private PipelineService pipelineService;
	
	@PostMapping
	public PipelineBean createPipeline(@RequestBody @Valid PipelineBean pipeline) throws IOException {
		
		return pipelineService.createPipeline(pipeline);
	}
	
	@GetMapping("/{pipelineId}")
	public PipelineBean getPipeline(@PathVariable @Min(1) Integer pipelineId) throws IOException {
		
		return pipelineService.getPipeline(pipelineId);
	}
	
	@DeleteMapping("/{pipelineId}")
	public void deletePipeline(@PathVariable @Min(1) Integer pipelineId) throws IOException {
		
		pipelineService.deletePipeline(pipelineId);
	}
}
