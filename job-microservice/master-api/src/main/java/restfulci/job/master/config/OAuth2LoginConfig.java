package restfulci.job.master.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/*
 * https://docs.spring.io/spring-security/site/docs/5.3.5.RELEASE/reference/html5/#oauth2resourceserver-jwt-minimalconfiguration
 * https://github.com/spring-projects/spring-security/tree/master/samples/boot/oauth2resourceserver-jwe
 */
@Configuration
public class OAuth2LoginConfig {
	
	@EnableWebSecurity
	public static class OAuth2LoginSecurityConfig extends WebSecurityConfigurerAdapter {

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http
				.authorizeRequests(authorize -> authorize
//						.antMatchers("/", "/error").permitAll()
						.anyRequest().authenticated()
						)
				.csrf().disable()
				.oauth2ResourceServer(oauth2 -> oauth2
						.jwt(jwt -> jwt
								.decoder(jwtDecoder())
								)
						);
		}
		
		public JwtDecoder jwtDecoder() {
			NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder
		    		.withJwkSetUri("http://localhost:8880/auth/realms/restfulci/protocol/openid-connect/certs")
		    		.build();
			jwtDecoder.setJwtValidator(
					JwtValidators.createDefaultWithIssuer("http://localhost:8880/auth/realms/restfulci"));
			return jwtDecoder;
		}
		
		/*
		 * Token generated from calling Keycloak endpoint `/e2e-api/test_auth.sh`.
		 * 
		 * TODO/questions:
		 * Why this application doesn't need to know clientId/secretId,
		 * but only the query to get the token need to know it?
		 */
	}
}