package restfulci.job.master.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import restfulci.job.master.config.source.KeycloakSource;

/*
 * https://docs.spring.io/spring-security/site/docs/5.3.5.RELEASE/reference/html5/#oauth2resourceserver-jwt-minimalconfiguration
 * https://github.com/spring-projects/spring-security/tree/master/samples/boot/oauth2resourceserver-jwe
 */
@Configuration
@EnableWebSecurity
public class OAuth2LoginConfig extends WebSecurityConfigurerAdapter {
	
	@Autowired private KeycloakSource keycloakSource;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.authorizeRequests(authorize -> authorize
//					.antMatchers("/", "/error").permitAll()
					.antMatchers(HttpMethod.OPTIONS).permitAll()
					.anyRequest().authenticated()
					)
			.csrf().disable()
			.oauth2ResourceServer(oauth2 -> oauth2
					.jwt(jwt -> jwt
							.decoder(jwtDecoder())
							)
					);
	}
	
	private JwtDecoder jwtDecoder() {
		NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder
				.withJwkSetUri(keycloakSource.getJwtSetUri())
				.build();
		jwtDecoder.setJwtValidator(
				JwtValidators.createDefaultWithIssuer(keycloakSource.getIssuerUri()));
		return jwtDecoder;
	}
	
	/*
	 * Token generated from calling Keycloak endpoint `/e2e-api/test_auth.sh`.
	 * 
	 * TODO/questions:
	 * Why this application doesn't need to know clientId/secretId,
	 * but only the query to get the token need to know it?
	 * 
	 * Also, there's a "Valid Redirect URLs" inside of Keycloak setup.
	 * However, it seems useless. Although the setup is 
	 * `localhost:8080` (dev) inside of it, it seems that there's no 
	 * problem if it is a different one.
	 */
}