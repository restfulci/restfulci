package restfulci.master.service;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import restfulci.master.dao.RunRepository;
import restfulci.master.domain.JobBean;
import restfulci.master.domain.RunBean;
import restfulci.master.dto.RunDTO;

@Service
public class RunServiceImpl implements RunService {
	
	@Autowired private RunRepository runRepository;

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
	public RunBean triggerRun(JobBean job, RunDTO runDTO) throws IOException {
		
		RunBean run = runDTO.toBean();
		run.setJob(job);
		runRepository.saveAndFlush(run);
		return run;
	}

}
