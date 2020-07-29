package restfulci.job.master.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import io.minio.errors.MinioException;
import restfulci.job.master.dto.RunDTO;
import restfulci.job.shared.domain.RunBean;
import restfulci.job.shared.domain.RunResultBean;

public interface RunService {
	
	public List<RunBean> listRunsByJob(Integer jobId, Integer page, Integer size) throws IOException;

	public RunBean getRun(Integer runId) throws IOException;
	public String getRunConfiguration(Integer runId) throws IOException, MinioException;
	public String getRunConsoleOutput(Integer runId) throws IOException, MinioException;
	
	public RunResultBean getRunResult(Integer runResultId) throws IOException;
	public InputStream getRunResultStream(Integer runResultId) throws IOException, MinioException;
	
	public RunBean triggerRun(Integer jobId, RunDTO runDTO) throws IOException, InterruptedException;
}
