package restfulci.master.service;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import io.minio.errors.MinioException;
import restfulci.master.dto.RunDTO;
import restfulci.shared.dao.MinioRepository;
import restfulci.shared.dao.RunRepository;
import restfulci.shared.domain.JobBean;
import restfulci.shared.domain.RunBean;

@Service
public class RunServiceImpl implements RunService {
	
	@Autowired private RunRepository runRepository;
	@Autowired private MinioRepository minioRepository;
	
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
	public String getRunConsoleOutput(Integer runId) throws IOException, MinioException {
		
		RunBean run = getRun(runId);
		InputStream stream = minioRepository.getRunOutput(run);
		return IOUtils.toString(stream, StandardCharsets.UTF_8.name());
	}
	
	@Override
	public RunBean triggerRun(JobBean job, RunDTO runDTO) throws IOException, InterruptedException {
		
		RunBean run = runDTO.toBean();
		run.setJob(job);
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

}
