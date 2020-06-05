package restfulci.job.master.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import io.minio.errors.MinioException;
import restfulci.job.master.dto.RunDTO;
import restfulci.job.shared.dao.MinioRepository;
import restfulci.job.shared.dao.RunRepository;
import restfulci.job.shared.dao.RunResultRepository;
import restfulci.job.shared.domain.GitRunBean;
import restfulci.job.shared.domain.JobBean;
import restfulci.job.shared.domain.JobType;
import restfulci.job.shared.domain.RunBean;
import restfulci.job.shared.domain.RunResultBean;
import restfulci.job.shared.exception.RunApiDataException;

@Service
@Transactional
public class RunServiceImpl implements RunService {
	
	@Autowired private RunRepository runRepository;
	@Autowired private RunResultRepository runResultRepository;
	@Autowired private MinioRepository minioRepository;
	
	@Autowired private JobService jobService;
	
	@Autowired private AmqpAdmin admin;
	@Autowired private AmqpTemplate template;

	@Override
	public RunBean getRun(Integer runId) throws IOException {
			
		Optional<RunBean> runs = runRepository.findById(runId);
		if (runs.isPresent()) {
			return runs.get();
		}
		else {
			throw new IOException();
		}
	}
	
	@Override
	public String getRunConfiguration(Integer runId) throws IOException, MinioException {
		
		RunBean run = getRun(runId);
		
		if (run instanceof GitRunBean) {
			GitRunBean gitRun = (GitRunBean)run;
			InputStream stream = minioRepository.getRunConfiguration(gitRun);
			return IOUtils.toString(stream, StandardCharsets.UTF_8.name());
		}
		else {
			/*
			 * TODO:
			 * Find out a way to record freestyle run configuration in database.
			 */
			return null;
		}
	}

	@Override
	public String getRunConsoleOutput(Integer runId) throws IOException, MinioException {
		
		RunBean run = getRun(runId);
		InputStream stream = minioRepository.getRunOutput(run);
		return IOUtils.toString(stream, StandardCharsets.UTF_8.name());
	}
	
	@Override
	public RunBean triggerRun(Integer jobId, RunDTO runDTO) throws IOException, InterruptedException {
		
		JobBean job = jobService.getJob(jobId);
		RunBean run = runDTO.toRunBean();
		
		if (!job.getType().equals(run.getType())) {
			if (job.getType().equals(JobType.FREESTYLE)) {
				throw new RunApiDataException("Run input doesn't match freestyle job.");
			}
			if (job.getType().equals(JobType.GIT)) {
				throw new RunApiDataException("Run input doesn't match git job.");
			}
		}
		
		run.setJob(job);
		
		/*
		 * Right now `fillInDefaultInput()` goes before `validateInput()`, 
		 * because we don't validate `defaultValue` is within `choices`.
		 * It can be validated in `fillInDefaultInput()` through.
		 */
		run.fillInDefaultInput();
		run.validateInput();
		
		/*
		 * No need to do it, as we are saving by `runRepository` rather
		 * than `jobRepository`.
		 * 
		 * This also help us to avoid the need of loading all runs belong
		 * to the same job. 
		 */
//		job.getRuns().add(run);
		runRepository.saveAndFlush(run);
		
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectWriter objectWriter = objectMapper.writer().withDefaultPrettyPrinter();
		String messageContent = objectWriter.writeValueAsString(run.toRunMessage());
		
		/*
		 * TODO:
		 * Unblocking way of sending messages to RabbitMQ. For example, consider 
		 * using Project Reactor https://projectreactor.io/:
		 * https://github.com/spring-cloud/spring-cloud-stream-samples/blob/bb0e21f6770722ecd7622912e44cb53bf203a66b/source-samples/dynamic-destination-source-rabbit/src/main/java/demo/DynamicDestinationSourceApplication.java#L61-L65
		 * 
		 * TODO:
		 * Better than this hard coded `queueName` which secretly follows Spring
		 * Cloud Stream's "Functional binding names" convention.
		 * https://cloud.spring.io/spring-cloud-static/spring-cloud-stream/3.0.2.RELEASE/reference/html/spring-cloud-stream.html#_functional_binding_names 
		 */
		String queueName = "executeRun-in-0.runqueue";
		admin.declareQueue(new Queue(queueName));
		template.convertAndSend(queueName, messageContent);
		
//		RunConfigBean runConfig;
//		if (run instanceof GitBranchRunBean) {
//			runConfig = remoteGitRepository.getConfig((GitBranchRunBean)run);
//		}
//		else if (run instanceof GitCommitRunBean) {
//			runConfig = remoteGitRepository.getConfig((GitCommitRunBean)run);
//		}
		
		return run;
	}
	
	@Override
	public RunResultBean getRunResult(Integer runResultId) throws IOException {
		
		Optional<RunResultBean> runResults = runResultRepository.findById(runResultId);
		if (runResults.isPresent()) {
			return runResults.get();
		}
		else {
			throw new IOException();
		}
	}
	
	@Override
	public InputStream getRunResultStream(Integer runResultId) throws IOException, MinioException {
		
		RunResultBean runResult = getRunResult(runResultId);
		InputStream stream = minioRepository.getRunResult(runResult);
		return stream;
	}
}
