package restfulci.job.shared.dao;

import java.io.InputStream;

import io.minio.errors.MinioException;
import restfulci.job.shared.domain.GitRunBean;
import restfulci.job.shared.domain.RunBean;
import restfulci.job.shared.domain.RunResultBean;

public interface MinioRepository {
	
	public String putRunOutputAndReturnObjectName(InputStream contentStream) throws MinioException;
	public String putRunOutputAndReturnObjectName(InputStream contentStream, String existingObjectName) throws MinioException;
	public InputStream getRunOutput(RunBean runBean) throws MinioException;
	public void removeRunOutput(RunBean runBean) throws MinioException;
	
	public String putRunConfigurationAndReturnObjectName(InputStream contentStream) throws MinioException;
	public String putRunConfigurationAndReturnObjectName(InputStream contentStream, String existingObjectName) throws MinioException;
	public InputStream getRunConfiguration(GitRunBean gitRunBean) throws MinioException;
	public void removeRunConfiguration(GitRunBean gitRunBean) throws MinioException;
	
	public String putRunResultAndReturnObjectName(InputStream contentStream) throws MinioException;
	public String putRunResultAndReturnObjectName(InputStream contentStream, String existingObjectName) throws MinioException;
	public InputStream getRunResult(RunResultBean runResultBean) throws MinioException;
	public void removeRunResult(RunResultBean runResultBean) throws MinioException;
}
