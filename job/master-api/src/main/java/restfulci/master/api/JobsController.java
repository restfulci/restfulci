package restfulci.master.api;

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

import restfulci.master.dto.JobDTO;
import restfulci.master.service.JobService;
import restfulci.shared.domain.JobBean;
import restfulci.shared.domain.ParameterBean;

@RestController
@RequestMapping(value="/jobs")
public class JobsController {

	@Autowired private JobService jobService;
	
	@PostMapping
	public JobBean createJob(@RequestBody @Valid JobDTO jobDTO) throws IOException {
		
		/*
		 * TODO:
		 * Should input a type, rather than completely rely on input content negotiation?
		 */
		return jobService.createJob(jobDTO);
	}
	
	@GetMapping("/{jobId}")
	public JobBean getJob(@PathVariable @Min(1) Integer jobId) throws IOException {
		
		return jobService.getJob(jobId);
	}
	
	@DeleteMapping("/{jobId}")
	public void deleteJob(@PathVariable @Min(1) Integer jobId) throws IOException {
		
		jobService.deleteJob(jobId);
	}
	
	@PostMapping("/{jobId}/parameters")
	public JobBean addParameter(
			@PathVariable @Min(1) Integer jobId,
			@RequestBody @Valid ParameterBean parameter) throws IOException {
		
		return jobService.addParameter(jobId, parameter);
	}
}
