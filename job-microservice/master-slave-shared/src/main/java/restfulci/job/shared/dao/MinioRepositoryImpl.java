package restfulci.job.shared.dao;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.MinioException;
import restfulci.job.shared.domain.BaseEntity;
import restfulci.job.shared.domain.GitRunBean;
import restfulci.job.shared.domain.RunBean;
import restfulci.job.shared.domain.RunResultBean;

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
			if (!minioClient.bucketExists(
					BucketExistsArgs
					.builder()
					.bucket(bucketName)
					.build())) {
				minioClient.makeBucket(
					MakeBucketArgs
					.builder()
					.bucket("my-bucketname")
					.build());
			}
		} catch (Exception e) {
			
		}
		
		try {
			minioClient.putObject(
					PutObjectArgs
					.builder()
					.bucket(bucketName)
					.object(objectName)
					.stream(contentStream, -1, 10485760)
					.build());
			
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
			return minioClient.getObject(
					GetObjectArgs
					.builder()
					.bucket(bucketName)
					.object(objectName)
					.build());
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
			minioClient.removeObject(
					RemoveObjectArgs
					.builder()
					.bucket(bucketName)
					.object(objectName)
					.build());
		} catch (InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String getOjectName(BaseEntity entity) {
		
		if (entity.getId() != null) {
			return entity.getId().toString();
		}
		else {
			/*
			 * This is for the situation the runBean has not been saved
			 * yet so it doesn't have an ID.
			 */
			return RandomStringUtils.random(10);
		}
	}

	@Override
	public void putRunOutputAndUpdateRunBean(RunBean runBean, InputStream contentStream) throws MinioException {
		
		String runOutputObjectName = getOjectName(runBean);
		minioPut(runOutputBucketName, runOutputObjectName, contentStream);
		runBean.setRunOutputObjectReferral(runOutputObjectName);
	}

	@Override
	public InputStream getRunOutput(RunBean runBean) throws MinioException {
		return minioGet(runOutputBucketName, runBean.getRunOutputObjectReferral());
	}

	@Override
	public void removeRunOutput(RunBean runBean) throws MinioException {
		minioRemove(runOutputBucketName, runBean.getRunOutputObjectReferral());
	}

	@Override
	public void putRunConfigurationAndUpdateRunBean(GitRunBean gitRunBean, InputStream contentStream) throws MinioException {
		
		String runConfigurationObjectName = getOjectName(gitRunBean);
		minioPut(runConfigurationBucketName, runConfigurationObjectName, contentStream);
		gitRunBean.setRunConfigurationObjectReferral(runConfigurationObjectName);
	}

	@Override
	public InputStream getRunConfiguration(GitRunBean gitRunBean) throws MinioException {
		return minioGet(runConfigurationBucketName, gitRunBean.getRunConfigurationObjectReferral());
	}

	@Override
	public void removeRunConfiguration(GitRunBean gitRunBean) throws MinioException {
		minioRemove(runConfigurationBucketName, gitRunBean.getRunConfigurationObjectReferral());
		gitRunBean.setRunConfigurationObjectReferral(null);
	}

	@Override
	public void putRunResultAndUpdateRunResultBean(RunResultBean runResultBean, InputStream contentStream) throws MinioException {
		
		String runResultObjectName = getOjectName(runResultBean);
		minioPut(runResultBucketName, runResultObjectName, contentStream);
		runResultBean.setObjectReferral(runResultObjectName);
	}

	@Override
	public InputStream getRunResult(RunResultBean runResultBean) throws MinioException {
		return minioGet(runResultBucketName, runResultBean.getObjectReferral());
	}

	@Override
	public void removeRunResult(RunResultBean runResultBean) throws MinioException {
		minioRemove(runResultBucketName, runResultBean.getObjectReferral());
		runResultBean.setObjectReferral(null);
	}

}
