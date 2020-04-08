package restfulci.shared.dao;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.io.input.NullInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import io.minio.MinioClient;
import io.minio.PutObjectOptions;
import io.minio.errors.MinioException;
import restfulci.shared.domain.RunBean;

@Repository
public class MinioRepositoryImpl implements MinioRepository {
	
	private final String runOutputBucketName = "run-output";
	
	@Autowired private MinioClient minioClient;
	
	private String getRunOutputObjectName(RunBean runBean) {
		return runBean.getId().toString();
	}

	@Override
	public void putRunOutputAndUpdateRunBean(RunBean runBean, InputStream contentStream) throws MinioException {
		
		/*
		 * TODO:
		 * Create bucket in initialization, rather than at run time.
		 */
		try {
			if (!minioClient.bucketExists(runOutputBucketName)) {
				minioClient.makeBucket(runOutputBucketName);
			}
		} catch (Exception e) {
			
		}
		
		try {
			minioClient.putObject(
					runOutputBucketName, 
					getRunOutputObjectName(runBean), 
					contentStream,
					new PutObjectOptions(-1, 5 * 1048576));
			runBean.setRunOutputObjectReferral(getRunOutputObjectName(runBean));
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

	@Override
	public InputStream getRunOutput(RunBean runBean) throws MinioException {
		
			try {
				return minioClient.getObject(
						runOutputBucketName, 
						getRunOutputObjectName(runBean));
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

	@Override
	public void removeRunOutput(RunBean runBean) throws MinioException {
		try {
			minioClient.removeObject(
					runOutputBucketName, 
					getRunOutputObjectName(runBean));
		} catch (InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
