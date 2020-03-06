package restfulci.master.service;

import java.io.IOException;

import restfulci.master.dto.RunDTO;
import restfulci.shared.domain.JobBean;
import restfulci.shared.domain.RunBean;

public interface RunService {

	public RunBean getRun(Integer runId) throws IOException;
	public RunBean triggerRun(JobBean job, RunDTO runDTO) throws IOException, InterruptedException;
}
