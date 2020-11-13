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

import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import restfulci.job.shared.dao.UserRepository;
import restfulci.job.shared.domain.UserBean;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class UserServiceTest {

	@Autowired private UserService userService;
	@Autowired private RestTemplate restTemplate;
	
	@MockBean private UserRepository userRepository;
	
	private MockRestServiceServer mockServer;
	private ObjectMapper objectMapper = new ObjectMapper();
	
	@BeforeEach
	public void init() {
		mockServer = MockRestServiceServer.createServer(restTemplate);
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
		
		given(userRepository.findByAuthId("0000-0000")).willReturn(new ArrayList<UserBean>());
		when(userRepository.saveAndFlush(any(UserBean.class))).then(returnsFirstArg());
		
		Map<String, Object> keyCloakUserinfo = new HashMap<String, Object>();
		keyCloakUserinfo.put("sub", "0000-0000");
		keyCloakUserinfo.put("email_verified", false);
		keyCloakUserinfo.put("preferred_username", "bar-user");
		
		mockServer.expect(ExpectedCount.once(), 
				requestTo("http://localhost:8880/auth/realms/restfulci/protocol/openid-connect/userinfo"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withStatus(HttpStatus.OK)
				.contentType(MediaType.APPLICATION_JSON)
				.body(objectMapper.writeValueAsString(keyCloakUserinfo))
			);
		
		UserBean user = userService.getUserByAuthId("0000-0000", "foo-token");
		assertEquals(user.getAuthId(), "0000-0000");
		assertEquals(user.getUsername(), "bar-user");
	}
}
