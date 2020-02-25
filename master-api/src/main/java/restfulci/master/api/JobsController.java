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

import lombok.extern.slf4j.Slf4j;
import restfulci.master.domain.JobBean;
import restfulci.master.dto.JobDTO;
import restfulci.master.service.JobService;

@RestController
@RequestMapping(value="/jobs")
@Slf4j
public class JobsController {

	@Autowired private JobService jobService;
	
	@PostMapping
	public JobBean createJob(@RequestBody @Valid JobDTO jobDTO) throws IOException {
		
		log.debug("Create job:"+jobDTO);
		
		/*
		 * TODO:
		 * Should input a type, rather than completely rely on input content negotiation?
		 */
		JobBean job = jobService.createJob(jobDTO);
		return job;
	}
	
	@GetMapping("/{jobId}")
	public JobBean getJob(@PathVariable @Min(1) Integer jobId) throws IOException {
		
		return jobService.getJob(jobId);
	}
	
	@DeleteMapping("/{jobId}")
	public void deleteJob(@PathVariable @Min(1) Integer jobId) throws IOException {
		
		JobBean job = jobService.getJob(jobId);
		jobService.deleteJob(job);
	}
}
