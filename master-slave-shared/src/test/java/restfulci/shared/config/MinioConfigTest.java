package restfulci.shared.config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.minio.MinioClient;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class MinioConfigTest {
	
	@Autowired private MinioClient minioClient;

	@Test
	public void testMinIOConnection() throws Exception {
		
		minioClient.makeBucket("testbucket");
		assertTrue(minioClient.bucketExists("testbucket"));
		minioClient.removeBucket("testbucket");
	}
}
