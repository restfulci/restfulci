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
import restfulci.job.shared.domain.GitRunBean;
import restfulci.job.shared.domain.RunBean;
import restfulci.job.shared.domain.RunResultBean;

@Repository
public class MinioRepositoryImpl implements MinioRepository {
	
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
					.bucket(bucketName)
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
	
	private String getRandomObjectName() {
		return RandomStringUtils.random(10);
	}

	private String putAndReturnObjectName(MinioBucket bucket, InputStream contentStream) throws MinioException {
		
		String objectName = getRandomObjectName();
		return putAndReturnObjectName(bucket, contentStream, objectName); 
	}

	private String putAndReturnObjectName(MinioBucket bucket, InputStream contentStream, String existingObjectName) throws MinioException {
		
		/*
		 * If `predefinedObjectName` does not exist yet, use (and return) a random one.
		 * Otherwise use the existing one. 
		 */
		if (existingObjectName == null) {
			return putAndReturnObjectName(bucket, contentStream);
		}
		
		minioPut(bucket.getBucketName(), existingObjectName, contentStream);
		return existingObjectName;
	}
	
	@Override
	public String putRunOutputAndReturnObjectName(InputStream contentStream) throws MinioException {
		return putAndReturnObjectName(MinioBucket.RUN_OUTPUT, contentStream);
	}

	@Override
	public String putRunOutputAndReturnObjectName(InputStream contentStream, String existingObjectName) throws MinioException {
		return putAndReturnObjectName(MinioBucket.RUN_OUTPUT, contentStream, existingObjectName);
	}

	@Override
	public InputStream getRunOutput(RunBean runBean) throws MinioException {
		return minioGet(MinioBucket.RUN_OUTPUT.getBucketName(), runBean.getRunOutputObjectReferral());
	}

	@Override
	public void removeRunOutput(RunBean runBean) throws MinioException {
		minioRemove(MinioBucket.RUN_OUTPUT.getBucketName(), runBean.getRunOutputObjectReferral());
	}
	
	@Override
	public String putRunConfigurationAndReturnObjectName(InputStream contentStream) throws MinioException {
		return putAndReturnObjectName(MinioBucket.RUN_CONFIGURATION, contentStream);
	}

	@Override
	public String putRunConfigurationAndReturnObjectName(InputStream contentStream, String existingObjectName) throws MinioException {
		return putAndReturnObjectName(MinioBucket.RUN_CONFIGURATION, contentStream, existingObjectName);
	}

	@Override
	public InputStream getRunConfiguration(GitRunBean gitRunBean) throws MinioException {
		return minioGet(MinioBucket.RUN_CONFIGURATION.getBucketName(), gitRunBean.getRunConfigurationObjectReferral());
	}

	@Override
	public void removeRunConfiguration(GitRunBean gitRunBean) throws MinioException {
		minioRemove(MinioBucket.RUN_CONFIGURATION.getBucketName(), gitRunBean.getRunConfigurationObjectReferral());
		gitRunBean.setRunConfigurationObjectReferral(null);
	}
	
	@Override
	public String putRunResultAndReturnObjectName(InputStream contentStream) throws MinioException {
		return putAndReturnObjectName(MinioBucket.RUN_RESULT, contentStream);
	}

	@Override
	public String putRunResultAndReturnObjectName(InputStream contentStream, String existingObjectName) throws MinioException {
		return putAndReturnObjectName(MinioBucket.RUN_RESULT, contentStream, existingObjectName);
	}

	@Override
	public InputStream getRunResult(RunResultBean runResultBean) throws MinioException {
		return minioGet(MinioBucket.RUN_RESULT.getBucketName(), runResultBean.getObjectReferral());
	}

	@Override
	public void removeRunResult(RunResultBean runResultBean) throws MinioException {
		minioRemove(MinioBucket.RUN_RESULT.getBucketName(), runResultBean.getObjectReferral());
		runResultBean.setObjectReferral(null);
	}
}
