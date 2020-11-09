package restfulci.job.master.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value="/users")
public class UsersController {
	
	/*
	 * https://github.com/spring-projects/spring-security/blob/b93528138e2b2f7ad34d10a040e6b7ac50fc587a/samples/boot/oauth2login-webflux/src/main/java/sample/web/OAuth2LoginController.java
	 */
	@GetMapping("/me")
	public Map<String, Object> getMe(
			Authentication authentication) throws IOException {
		
		/*
		 * TODO:
		 * Current shown username is Keycloak ID. To retrieve the real username
		 * we'll need to query auth server. We should setup a user table in 
		 * this microservice, and sync the database between those two services.
		 */
		Map<String, Object> me = new HashMap<String, Object>();
		me.put("username", authentication.getName());
		return me;
	}
}
