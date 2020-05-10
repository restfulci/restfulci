package restfulci.shared.dao;

import java.io.InputStream;

import io.minio.errors.MinioException;
import restfulci.shared.domain.GitRunBean;
import restfulci.shared.domain.RunBean;
import restfulci.shared.domain.RunResultBean;

public interface MinioRepository {

	public void putRunOutputAndUpdateRunBean(RunBean runBean, InputStream contentStream) throws MinioException;
	public InputStream getRunOutput(RunBean runBean) throws MinioException;
	public void removeRunOutput(RunBean runBean) throws MinioException;
	
	public void putRunConfigurationAndUpdateRunBean(GitRunBean gitRunBean, InputStream contentStream) throws MinioException;
	public InputStream getRunConfiguration(GitRunBean gitRunBean) throws MinioException;
	public void removeRunConfiguration(GitRunBean gitRunBean) throws MinioException;
	
	public void putRunResultAndUpdateRunResultBean(RunResultBean runResultBean, InputStream contentStream) throws MinioException;
	public InputStream getRunResult(RunResultBean runResultBean) throws MinioException;
	public void removeRunResult(RunResultBean runResultBean) throws MinioException;
}
