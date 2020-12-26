package restfulci.job.shared.config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.RemoveBucketArgs;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class MinioConfigTest {
	
	@Autowired private MinioClient minioClient;

	@Test
	public void testMinIOConnection() throws Exception {
		
		minioClient.makeBucket(
				MakeBucketArgs
				.builder()
				.bucket("testbucket")
				.build());
		assertTrue(minioClient.bucketExists(
				BucketExistsArgs
				.builder()
				.bucket("testbucket")
				.build()));
		minioClient.removeBucket(
				RemoveBucketArgs
				.builder()
				.bucket("testbucket")
				.build());
	}
}
