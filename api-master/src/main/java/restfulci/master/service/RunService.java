package restfulci.master.service;

import java.io.IOException;

import restfulci.master.domain.JobBean;
import restfulci.master.domain.RunBean;
import restfulci.master.dto.RunDTO;

public interface RunService {

	public RunBean getRun(Integer runId) throws IOException;
	public RunBean triggerRun(JobBean job, RunDTO runDTO) throws IOException;
}
