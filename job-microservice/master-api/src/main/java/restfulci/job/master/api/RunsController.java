package restfulci.job.master.api;

import java.io.IOException;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import restfulci.job.master.dto.RunDTO;
import restfulci.job.master.service.RunService;
import restfulci.job.master.service.UserService;
import restfulci.job.shared.domain.RunBean;
import restfulci.job.shared.domain.UserBean;

@RestController
@RequestMapping(value="/jobs/{jobId}/runs")
public class RunsController {

	@Autowired private RunService runService;
	@Autowired private UserService userService;
	
	@GetMapping
	public List<RunBean> listRuns(
			@PathVariable @Min(1) Integer jobId,
			@RequestParam(required=false) Integer page,
			@RequestParam(required=false) Integer size) throws IOException {
		
		if (page == null) {
			page = 1;
		}
		
		if (size == null) {
			size = 10;
		}
		
		return runService.listRunsByJob(jobId, page, size);
	}
	
	/*
	 * TODO:
	 * Consider to use `JsonNode` as @RequestBody, as we need to accept
	 * wildcard JSON attributes to generate `InputBean`s.
	 * https://medium.com/@saibaburvr/spring-rest-jacksons-jsonnode-for-payload-unaware-request-handling-25a09e2b1ef5
	 */
	@PostMapping
	public RunBean triggerRun(
			@PathVariable @Min(1) Integer jobId,
			@RequestBody @Valid RunDTO runDTO,
			Authentication authentication,
			@RequestHeader(name="Authorization") String token) throws Exception {
		
		UserBean user = userService.getUserByAuthId(
				authentication.getName(),
				token);
		return runService.triggerRun(jobId, runDTO, user);
	}
	
	@GetMapping("/{runId}")
	public RunBean getRun(@PathVariable @Min(1) Integer runId) throws IOException {
		
		return runService.getRun(runId);
	}
	
	/*
	 * May consider return YAML mime type.
	 * Ruby on rails uses `application/x-yaml`: https://stackoverflow.com/questions/332129/yaml-mime-type
	 * Unfortunately there's no supporting type in Spring yet: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/http/MediaType.html
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
