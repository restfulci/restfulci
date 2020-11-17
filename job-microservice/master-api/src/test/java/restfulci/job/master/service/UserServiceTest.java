package restfulci.job.master.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.ExpectedCount;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import restfulci.job.shared.dao.UserRepository;
import restfulci.job.shared.domain.UserBean;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class UserServiceTest {

	@Autowired private UserService userService;
	
	@MockBean private UserRepository userRepository;
	
	/*
{
 "exp": 2037590243,
 "iat": 1605590243,
 "jti": "a1f2daa3-1b7b-472d-a2b9-68043959f5f6",
 "iss": "http://localhost:8880/auth/realms/restfulci",
 "aud": "account",
 "sub": "a8321a8b-5fe6-4aa9-8612-d3ed29af2853",
 "typ": "Bearer",
 "azp": "job-microservice",
 "session_state": "ee47f162-58d5-4721-be46-1fc8f0f1cbe5",
 "acr": "1",
 "allowed-origins": [
  "*"
 ],
 "realm_access": {
  "roles": [
   "offline_access",
   "uma_authorization"
  ]
 },
 "resource_access": {
  "account": {
   "roles": [
    "manage-account",
    "manage-account-links",
    "view-profile"
   ]
  }
 },
 "scope": "profile email",
 "email_verified": false,
 "preferred_username": "test-user"
}
	 */
	private final String thirteenYearsExpToken = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICIxV0UwbXBwcEdMeDZUYzVGMXRFVW1KS0UzdGtIX2NnNmRvSGhMRzdUVkVzIn0.eyJleHAiOjIwMzc1OTAyNDMsImlhdCI6MTYwNTU5MDI0MywianRpIjoiYTFmMmRhYTMtMWI3Yi00NzJkLWEyYjktNjgwNDM5NTlmNWY2IiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4ODgwL2F1dGgvcmVhbG1zL3Jlc3RmdWxjaSIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiJhODMyMWE4Yi01ZmU2LTRhYTktODYxMi1kM2VkMjlhZjI4NTMiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJqb2ItbWljcm9zZXJ2aWNlIiwic2Vzc2lvbl9zdGF0ZSI6ImVlNDdmMTYyLTU4ZDUtNDcyMS1iZTQ2LTFmYzhmMGYxY2JlNSIsImFjciI6IjEiLCJhbGxvd2VkLW9yaWdpbnMiOlsiKiJdLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwicHJlZmVycmVkX3VzZXJuYW1lIjoidGVzdC11c2VyIn0.GWzpeybEiFxXVQcSy-mqUdS1PmpvjKGcMEm77tUg0v_N56z5qOV3I0__8-bSvohOIlkcIXnwuXRkIuDFI6kKC0NHJzbFewoCoPPfrd6hEyQ9urnmEjqpe70Z6Gg2y24Z9WEk9NFXPWpRvXVnBOHtOBzxHGdIB0wqjlBlepXmLFxnm9BK1vkWQI3QDUKPt6HaNNtLbVEcY7jbAWutIHNLBdu66clEXxAwq7uSPeG_bzH0zL2qwzlzxFipPx6rRQwmaqzjU9Jzr6Qm47Et1UCS_xWfG5cjhMunenbCsoK0rdKOCUxD_lAVz1HCksxaXkoOc-L0_KjDBFZHVANoKKDdaA";
	
	@Test
	public void testDecodeJwtToken() throws Exception {
		
		/*
		 * Setup a token which will expire in year 2034.
		 */
		String withoutSignature = thirteenYearsExpToken.substring(0, thirteenYearsExpToken.lastIndexOf('.') + 1);
		Jwt<Header, Claims> untrusted = Jwts.parser().parseClaimsJwt(withoutSignature);
		assertEquals(untrusted.getBody().get("preferred_username"), "test-user");
	}
	
	@Test
	public void testGetUserByAuthIdWhenExist() throws Exception {

		UserBean user = new UserBean();

		given(userRepository.findByAuthId("0000-0000")).willReturn(Arrays.asList(user));

		assertEquals(
			userService.getUserByAuthId("0000-0000", "foo"),
			user);	
	}
	
	@Test
	public void testGetUserByAuthIdWhenNot() throws Exception {

		given(userRepository.findByAuthId("a8321a8b-5fe6-4aa9-8612-d3ed29af2853")).willReturn(new ArrayList<UserBean>());
		when(userRepository.saveAndFlush(any(UserBean.class))).then(returnsFirstArg());

		UserBean user = userService.getUserByAuthId("a8321a8b-5fe6-4aa9-8612-d3ed29af2853", thirteenYearsExpToken);
		assertEquals(user.getAuthId(), "a8321a8b-5fe6-4aa9-8612-d3ed29af2853");
		assertEquals(user.getUsername(), "test-user");
	}
}
