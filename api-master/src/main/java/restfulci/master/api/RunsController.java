package restfulci.master.api;

import java.io.IOException;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import restfulci.master.domain.JobBean;
import restfulci.master.domain.RunBean;
import restfulci.master.dto.RunDTO;
import restfulci.master.service.JobService;
import restfulci.master.service.RunService;

@RestController
@RequestMapping(value="/jobs/{jobId}/runs")
public class RunsController {

	@Autowired private JobService jobService;
	@Autowired private RunService runService;
	
	@PostMapping
	public RunBean triggerRun(
			@PathVariable @Min(1) Integer jobId,
			@RequestBody @Valid RunDTO runDTO) throws IOException {
		
		JobBean job = jobService.getJob(jobId);
		return runService.triggerRun(job, runDTO);
	}
	
	@GetMapping("/{runId}")
	public RunBean getRun(@PathVariable @Min(1) Integer runId) throws IOException {

		return runService.getRun(runId);
	}
}
