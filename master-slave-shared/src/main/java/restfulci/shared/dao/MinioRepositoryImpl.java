package restfulci.shared.dao;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.io.input.NullInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import io.minio.MinioClient;
import io.minio.PutObjectOptions;
import io.minio.errors.MinioException;
import restfulci.shared.domain.GitRunBean;
import restfulci.shared.domain.RunBean;
import restfulci.shared.domain.RunResultBean;

@Repository
public class MinioRepositoryImpl implements MinioRepository {
	
	/*
	 * TODO:
	 * Should we move the bucket name definition, and the referral rule
	 * to the corresponding domain method?
	 */
	private final String runOutputBucketName = "run-output";
	private final String runConfigurationBucketName = "run-configuration";
	private final String runResultBucketName = "run-result";
	
	@Autowired private MinioClient minioClient;
	
	private void minioPut(String bucketName, String objectName, InputStream contentStream) throws MinioException {
		
		/*
		 * TODO:
		 * Create bucket in initialization, rather than at run time.
		 */
		try {
			if (!minioClient.bucketExists(bucketName)) {
				minioClient.makeBucket(bucketName);
			}
		} catch (Exception e) {
			
		}
		
		try {
			minioClient.putObject(
					bucketName, 
					objectName, 
					contentStream,
					new PutObjectOptions(-1, 5 * 1048576));
			
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private InputStream minioGet(String bucketName, String objectName) throws MinioException {
		
		try {
			return minioClient.getObject(bucketName, objectName);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return new NullInputStream(0);
	}
	
	private void minioRemove(String bucketName, String objectName) throws MinioException {
		
		try {
			minioClient.removeObject(bucketName, objectName);
		} catch (InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String getRunOutputObjectName(RunBean runBean) {
		return runBean.getId().toString();
	}

	@Override
	public void putRunOutputAndUpdateRunBean(RunBean runBean, InputStream contentStream) throws MinioException {
		minioPut(runOutputBucketName, getRunOutputObjectName(runBean), contentStream);
		runBean.setRunOutputObjectReferral(getRunOutputObjectName(runBean));
	}

	@Override
	public InputStream getRunOutput(RunBean runBean) throws MinioException {
		return minioGet(runOutputBucketName, getRunOutputObjectName(runBean));
	}

	@Override
	public void removeRunOutput(RunBean runBean) throws MinioException {
		minioRemove(runOutputBucketName, getRunOutputObjectName(runBean));
	}
	
	private String getRunConfigurationObjectName(GitRunBean runBean) {
		return runBean.getId().toString();
	}

	@Override
	public void putRunConfigurationAndUpdateRunBean(GitRunBean gitRunBean, InputStream contentStream) throws MinioException {
		minioPut(runConfigurationBucketName, getRunConfigurationObjectName(gitRunBean), contentStream);
		gitRunBean.setRunConfigurationObjectReferral(getRunConfigurationObjectName(gitRunBean));
	}

	@Override
	public InputStream getRunConfiguration(GitRunBean gitRunBean) throws MinioException {
		return minioGet(runConfigurationBucketName, getRunConfigurationObjectName(gitRunBean));
	}

	@Override
	public void removeRunConfiguration(GitRunBean gitRunBean) throws MinioException {
		minioRemove(runConfigurationBucketName, getRunConfigurationObjectName(gitRunBean));
		gitRunBean.setRunConfigurationObjectReferral(null);
	}
	
	private String getRunResultObjectName(RunResultBean runResultBean) {
		return runResultBean.getId().toString();
	}

	@Override
	public void putRunResultAndUpdateRunResultBean(RunResultBean runResultBean, InputStream contentStream) throws MinioException {
		minioPut(runResultBucketName, getRunResultObjectName(runResultBean), contentStream);
		runResultBean.setObjectReferral(getRunResultObjectName(runResultBean));
	}

	@Override
	public InputStream getRunResult(RunResultBean runResultBean) throws MinioException {
		return minioGet(runResultBucketName, getRunResultObjectName(runResultBean));
	}

	@Override
	public void removeRunResult(RunResultBean runResultBean) throws MinioException {
		minioRemove(runResultBucketName, getRunResultObjectName(runResultBean));
		runResultBean.setObjectReferral(null);
	}

}
