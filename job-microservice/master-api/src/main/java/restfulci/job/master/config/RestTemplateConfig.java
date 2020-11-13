package restfulci.job.master.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

/*
 * TODO:
 * More specific `AuthMicroserviceRestTemplate`.
 */
@Configuration
public class RestTemplateConfig {

	@Profile("dev")
	@Bean 
	RestTemplate devRestTemplate(RestTemplateBuilder builder) {
		return builder.rootUri("http://localhost:8880").build();
	}
	
	@Profile("docker")
	@Bean 
	RestTemplate dockerRestTemplate(RestTemplateBuilder builder) {
		return builder.rootUri("http://keycloak:8080").build();
	}
	
	@Profile("circleci")
	@Bean 
	RestTemplate circleciRestTemplate(RestTemplateBuilder builder) {
		return builder.rootUri("http://localhost:8880").build();
	}
}
