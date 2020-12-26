package restfulci.job.shared.config;

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
		return MinioClient.builder()
				.endpoint("http://localhost:9000")
				.credentials("restfulci", "secretpassword")
				.build();
	}
	
	@Profile("docker")
	@Bean
	public MinioClient dockerMinioClient() throws MinioException {
		return MinioClient.builder()
				.endpoint("http://job-minio:9000")
				.credentials("restfulci", "secretpassword")
				.build();
	}
	
	@Profile("kubernetes")
	@Bean
	public MinioClient kubernetesMinioClient() throws MinioException {
		return MinioClient.builder()
				.endpoint("http://restfulci-job-minio:9000")
				.credentials("restfulci", "secretpassword")
				.build();
	}
	
	@Profile("circleci")
	@Bean
	public MinioClient circleciMinioClient() throws MinioException {
		return MinioClient.builder()
				.endpoint("http://localhost:9000")
				.credentials("restfulci", "secretpassword")
				.build();
	}
}