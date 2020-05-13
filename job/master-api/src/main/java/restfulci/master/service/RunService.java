package restfulci.master.service;

import java.io.IOException;
import java.io.InputStream;

import io.minio.errors.MinioException;
import restfulci.master.dto.RunDTO;
import restfulci.shared.domain.RunBean;
import restfulci.shared.domain.RunResultBean;

public interface RunService {

	public RunBean getRun(Integer runId) throws IOException;
	public String getRunConfiguration(Integer runId) throws IOException, MinioException;
	public String getRunConsoleOutput(Integer runId) throws IOException, MinioException;
	
	public RunResultBean getRunResult(Integer runResultId) throws IOException;
	public InputStream getRunResultStream(Integer runResultId) throws IOException, MinioException;
	
	public RunBean triggerRun(Integer jobId, RunDTO runDTO) throws IOException, InterruptedException;
}
