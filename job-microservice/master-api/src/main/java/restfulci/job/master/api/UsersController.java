package restfulci.job.master.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.ui.Model;
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
	public Map<String, Object> getJob(
			Model model,
			@RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient,
			@AuthenticationPrincipal OAuth2User oauth2User) throws IOException {
		
		Map<String, Object> me = new HashMap<String, Object>();
		me.put("username", oauth2User.getName());
		me.put("clientName", authorizedClient.getClientRegistration().getClientName());
		me.put("userAttributes", oauth2User.getAttributes());
		return me;
	}
}
