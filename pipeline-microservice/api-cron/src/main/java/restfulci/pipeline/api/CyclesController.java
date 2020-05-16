package restfulci.pipeline.api;

import javax.validation.constraints.Min;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import restfulci.pipeline.domain.CycleBean;
import restfulci.pipeline.service.CycleService;

@RestController
@RequestMapping(value="/pipelines/{pipelineId}/cycles")
public class CyclesController {

	@Autowired private CycleService cycleService;
	
	@PostMapping
	public CycleBean triggerCycle(@PathVariable @Min(1) Integer pipelineId) throws Exception {
		
		return cycleService.triggerCycle(pipelineId);
	}
	
	@GetMapping("/{cycleId}")
	public CycleBean getPipeline(
			@PathVariable @Min(1) Integer pipelineId,
			@PathVariable @Min(1) Integer cycleId) throws Exception {
		
		return cycleService.getCycle(cycleId);
	}
	
	@DeleteMapping("/{cycleId}")
	public void deletePipeline(
			@PathVariable @Min(1) Integer pipelineId,
			@PathVariable @Min(1) Integer cycleId) throws Exception {
		
		cycleService.deleteCycle(cycleId);
	}	
}
