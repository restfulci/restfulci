package restfulci.pipeline.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

	@Profile("dev")
	@Bean 
	RestTemplate devRestTemplate(RestTemplateBuilder builder) {
		return builder.rootUri("http://localhost:5000").build();
	}
	
	@Profile("docker")
	@Bean 
	RestTemplate dockerRestTemplate(RestTemplateBuilder builder) {
		return builder.rootUri("http://job-microservice-mock:5000").build();
	}
	
	@Profile("kubernetes")
	@Bean 
	RestTemplate kubernetesRestTemplate(RestTemplateBuilder builder) {
		return builder.rootUri("job.restfulci-job-master-api-server:80").build();
	}
	
	@Profile("circleci")
	@Bean 
	RestTemplate circleciRestTemplate(RestTemplateBuilder builder) {
		return builder.rootUri("http://localhost:5000").build();
	}
}
