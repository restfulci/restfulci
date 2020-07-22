package restfulci.job.master.api;

import java.io.IOException;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import restfulci.job.master.dto.JobDTO;
import restfulci.job.master.service.JobService;
import restfulci.job.shared.domain.JobBean;
import restfulci.job.shared.domain.ParameterBean;

@RestController
@RequestMapping(value="/jobs")
public class JobsController {

	@Autowired private JobService jobService;
	
	@GetMapping
	public List<JobBean> listJobs(
			@RequestParam(required=false) Integer page,
			@RequestParam(required=false) Integer size) {
		
		if (page == null) {
			page = 1;
		}
		
		if (size == null) {
			size = 10;
		}
		
		return jobService.listJobs(page, size);
	}
	
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
