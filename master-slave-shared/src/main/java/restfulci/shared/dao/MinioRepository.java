package restfulci.shared.dao;

import java.io.InputStream;

import io.minio.errors.MinioException;
import restfulci.shared.domain.RunBean;

public interface MinioRepository {

	public void putRunOutputAndUpdateRunBean(RunBean runBean, InputStream contentStream) throws MinioException;
	public InputStream getRunOutput(RunBean runBean) throws MinioException;
	public void removeRunOutput(RunBean runBean) throws MinioException;
}
