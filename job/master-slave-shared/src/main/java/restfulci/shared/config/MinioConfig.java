package restfulci.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.minio.MinioClient;
import io.minio.errors.MinioException;

@Configuration
public class MinioConfig {
	
	@Profile("dev")
	@Bean
	public MinioClient devMinioClient() throws MinioException {
		return new MinioClient(
				"http://localhost:9000",
				"restfulci", 
				"secretpassword");
	}
	
	@Profile("docker")
	@Bean
	public MinioClient dockerMinioClient() throws MinioException {
		return new MinioClient(
				"http://minio:9000",
				"restfulci", 
				"secretpassword");
	}
	
	@Profile("kubernetes")
	@Bean
	public MinioClient kubernetesMinioClient() throws MinioException {
		return new MinioClient(
				"http://restfulci-job-minio:9000",
				"restfulci", 
				"secretpassword");
	}
	
	@Profile("circleci")
	@Bean
	public MinioClient circleciMinioClient() throws MinioException {
		return new MinioClient(
				"http://localhost:9000",
				"restfulci", 
				"secretpassword");
	}
}