package restfulci.master.service;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import restfulci.master.dao.RunConfigRepository;
import restfulci.master.dao.RunRepository;
import restfulci.master.domain.GitBranchRunBean;
import restfulci.master.domain.GitCommitRunBean;
import restfulci.master.domain.JobBean;
import restfulci.master.domain.RunBean;
import restfulci.master.domain.yaml.RunConfigBean;
import restfulci.master.dto.RunDTO;

@Service
public class RunServiceImpl implements RunService {
	
	@Autowired private RunRepository runRepository;
	@Autowired private RunConfigRepository runConfigRepository;

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
	public RunBean triggerRun(JobBean job, RunDTO runDTO) throws IOException, InterruptedException {
		
		RunBean run = runDTO.toBean();
		run.setJob(job);
		runRepository.saveAndFlush(run);
		
		/*
		 * TODO:
		 * Should this part be put into a message queue?
		 */
//		RunConfigBean runConfig;
//		if (run instanceof GitBranchRunBean) {
//			runConfig = runConfigRepository.getConfig((GitBranchRunBean)run);
//		}
//		else if (run instanceof GitCommitRunBean) {
//			runConfig = runConfigRepository.getConfig((GitCommitRunBean)run);
//		}
		
		return run;
	}

}
