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
		 * TODO:
		 * There's a problem that if we use `keycloak:8080` as the issuer,
		 * we need to go into the master-api-server container and get the 
		 * JWT using the same URL. Get it from the outside (`localhost:8880`)
		 * gives a 
		 * > WWW-Authenticate: Bearer error="invalid_token", error_description="An error occurred while attempting to decode the Jwt: Signed JWT rejected: Invalid signature", error_uri="https://tools.ietf.org/html/rfc6750#section-3.1"
		 * 
		 * It should be resolved by Keycloak `frontendUrl`:
		 * https://www.keycloak.org/docs/latest/server_installation/#default-provider
		 * This thread may also help:
		 * https://github.com/louketo/louketo-proxy/issues/539
		 * but a rough try seems just redirect whatever in browser URL window
		 * to the setup one, but didn't resolve the problem.
		 * > /opt/jboss/keycloak/bin/jboss-cli.sh --connect --command='/subsystem=keycloak-server/spi=hostname/provider=default:write-attribute(name=properties.frontendUrl,value="http://localhost:8880/auth")'
		 * 
		 * Another possible workaround should be setup a set of endpoints to 
		 * proxy curl Keycloak to get the token, but I don't know if there
		 * are other solutions which makes more sense.
		 */
		keycloakSource.setIssuerUri("http://keycloak:8080/auth/realms/restfulci");
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
