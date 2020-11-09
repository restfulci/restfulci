package restfulci.job.master.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import restfulci.job.master.config.source.KeycloakSource;

@Configuration
public class KeycloakConfig {

	@Profile("dev")
	@Bean
	public KeycloakSource devKeycloakSource() {
		KeycloakSource keycloakSource = new KeycloakSource();
		keycloakSource.setIssuerUri("http://localhost:8880/auth/realms/restfulci");
		keycloakSource.setJwtSetUri("http://localhost:8880/auth/realms/restfulci/protocol/openid-connect/certs");
		return keycloakSource;
	}
	
	@Profile("docker")
	@Bean
	public KeycloakSource dockerKeycloakSource() {
		KeycloakSource keycloakSource = new KeycloakSource();
		
		/*
		 * The issuer URI need to be the URL user access the Keycloak server.
		 * In docker-compose world, user is accessing Keycloak server from port
		 * forwarding (so it is localhost:<port>).
		 * 
		 * Resource server is decoding the JWT token, read the issuer URI,
		 * and compare it to the one defines here (we can use e.g. 
		 * https://www.jsonwebtoken.io/ to check the decoded issuer from JWT
		 * token). If it doesn't match, it returns:
		 * > WWW-Authenticate: Bearer error="invalid_token", error_description="An error occurred while attempting to decode the Jwt: Signed JWT rejected: Invalid signature", error_uri="https://tools.ietf.org/html/rfc6750#section-3.1"
		 */
		keycloakSource.setIssuerUri("http://localhost:8880/auth/realms/restfulci");
		keycloakSource.setJwtSetUri("http://keycloak:8080/auth/realms/restfulci/protocol/openid-connect/certs");
				
		return keycloakSource;
	}
	
	@Profile("circleci")
	@Bean
	public KeycloakSource circleciKeycloakSource() {
		KeycloakSource keycloakSource = new KeycloakSource();
		keycloakSource.setIssuerUri("http://foo");
		keycloakSource.setJwtSetUri("http://bar");
		return keycloakSource;
	}
}
