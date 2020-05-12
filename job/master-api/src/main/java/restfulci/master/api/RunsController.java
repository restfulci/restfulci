package restfulci.master.api;

import java.io.IOException;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import restfulci.master.dto.RunDTO;
import restfulci.master.service.JobService;
import restfulci.master.service.RunService;
import restfulci.shared.domain.JobBean;
import restfulci.shared.domain.RunBean;

@RestController
@RequestMapping(value="/jobs/{jobId}/runs")
public class RunsController {

	@Autowired private JobService jobService;
	@Autowired private RunService runService;
	
	/*
	 * TODO:
	 * Consider to use `JsonNode` as @RequestBody, as we need to accept
	 * wildcard JSON attributes to generate `InputBean`s.
	 * https://medium.com/@saibaburvr/spring-rest-jacksons-jsonnode-for-payload-unaware-request-handling-25a09e2b1ef5
	 * 
	 * TODO:
	 * Should it be @Transactional?
	 */
	@PostMapping
	public RunBean triggerRun(
			@PathVariable @Min(1) Integer jobId,
			@RequestBody @Valid RunDTO runDTO) throws Exception {
		
		JobBean job = jobService.getJob(jobId);
		return runService.triggerRun(job, runDTO);
	}
	
	@GetMapping("/{runId}")
	public RunBean getRun(@PathVariable @Min(1) Integer runId) throws IOException {
		
		return runService.getRun(runId);
	}
	
	/*
	 * TODO:
	 * Return YAML mime type?
	 * https://stackoverflow.com/questions/332129/yaml-mime-type
	 */
	@GetMapping("/{runId}/configuration")
	public String getRunConfiguration(@PathVariable @Min(1) Integer runId) throws Exception {
	
		return runService.getRunConfiguration(runId);
	}
	
	@GetMapping("/{runId}/console")
	public String getRunConsoleOutput(@PathVariable @Min(1) Integer runId) throws Exception {
	
		return runService.getRunConsoleOutput(runId);
	}
	
	@GetMapping("/{runId}/results/{runResultId}")
	public ResponseEntity<Resource> getRunResult(
			@PathVariable @Min(1) Integer runId,
			@PathVariable @Min(1) Integer runResultId) throws Exception {
	
		InputStreamResource resource = new InputStreamResource(
				runService.getRunResultStream(runResultId));

		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType("application/zip"))
				.body(resource);
	}
}
