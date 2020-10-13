package restfulci.job.master.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

/*
 * https://docs.spring.io/spring-security/site/docs/5.3.5.RELEASE/reference/html5/#oauth2login-override-boot-autoconfig
 * https://medium.com/@hantsy/secures-your-application-with-spring-security-5-and-keycloak-2804ee912b0f
 * https://github.com/thomasdarimont/spring-boot-2-keycloak-oauth-example/blob/master/src/main/resources/application.yml
 * https://github.com/spring-projects/spring-security/tree/master/samples/boot/oauth2login
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
				.oauth2Login(withDefaults());
		}
	}

	@Bean
	public ClientRegistrationRepository clientRegistrationRepository() {
		return new InMemoryClientRegistrationRepository(this.keycloakClientRegistration());
	}

	private ClientRegistration keycloakClientRegistration() {
		return ClientRegistration.withRegistrationId("keycloak")
			.clientId("job-microservice")
			.clientSecret("804ccb10-850e-40d9-aae5-9e69b91af511")
			.clientAuthenticationMethod(ClientAuthenticationMethod.BASIC)
			.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
			.redirectUriTemplate("{baseUrl}/login/oauth2/code/{registrationId}")
			.scope("openid", "profile", "email")
			.authorizationUri("http://localhost:8880/auth/realms/restfulci/protocol/openid-connect/auth")
			.tokenUri("http://localhost:8880/auth/realms/restfulci/protocol/openid-connect/token")
			.userInfoUri("http://localhost:8880/auth/realms/restfulci/protocol/openid-connect/userinfo")
			.userNameAttributeName("preferred_username")
			//.userNameAttributeName(IdTokenClaimNames.SUB)
			.jwkSetUri("http://localhost:8880/auth/realms/restfulci/protocol/openid-connect/certs")
			.clientName("Keycloak")
			.build();
	}
}