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
		/*
		 * TODO:
		 * This doesn't work. 
		 * 
		 * Previously we were facing a problem that when
		 * the token is issued from outside (call localhost:8880), 
		 * job-microservice (the resource server) doesn't treat it
		 * as valid. It is resolved by changing job-microservice 
		 * issuer URI to be the one see from outside. See
		 * https://github.com/restfulci/restfulci/commit/34836ecbac3aa6cbbd3f26a6b0e51c53fe96b615
		 * 
		 * Here we are calling KeyCloak as the resource server to
		 * get the user info. The token is from the outside, and is
		 * passing to KeyCloak. To decode the token the issuer URI
		 * is localhost:8880, but from keycloak it should be 
		 * keycloak:8080. So it erros out with:
		 * > master-api-server_1  | 2020-11-13 05:49:47.779 ERROR 1 --- [nio-8080-exec-3] o.s.b.w.servlet.support.ErrorPageFilter  : Forwarding to error page from request [/jobs/1/runs] due to exception [401 Unauthorized: [{"error":"invalid_token","error_description":"Token verification failed"}]]
		 * > master-api-server_1  |
		 * > master-api-server_1  | org.springframework.web.client.HttpClientErrorException$Unauthorized: 401 Unauthorized: [{"error":"invalid_token","error_description":"Token verification failed"}]
		 * 
		 * The fix should be some kind of setup Keycloak (as a 
		 * user info resource server) issuer URL to be localhost:8880
		 * instead of keycloak:8080. But I am not clear how to
		 * achieve that.
		 */
		return builder.rootUri("http://keycloak:8080").build();
	}
	
	@Profile("circleci")
	@Bean 
	RestTemplate circleciRestTemplate(RestTemplateBuilder builder) {
		return builder.rootUri("http://localhost:8880").build();
	}
}
