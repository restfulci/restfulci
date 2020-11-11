package restfulci.job.master.service;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import restfulci.job.master.dto.KeycloakUserDTO;
import restfulci.job.shared.dao.UserRepository;
import restfulci.job.shared.domain.UserBean;

@Service
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {
	
	@Autowired private UserRepository userRepository;
	
	@Autowired private RestTemplate restTemplate;

	@Override
	public UserBean getUserByAuthId(String authId, String token) throws IOException {
		
		List<UserBean> users = userRepository.findByAuthId(authId);
		if (users.size() > 1) {
			throw new RuntimeException("User auth ID should be unique");
		}
		else if (users.size() == 1) {
			return users.get(0);
		}
		else {
			UserBean user = queryUserBeanFromKeycloak(token);
			return createUser(user);
		}
	}
	
	private UserBean queryUserBeanFromKeycloak(String token) throws IOException {
		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION, token);
		ResponseEntity<String> response = restTemplate.exchange(
				"/auth/realms/restfulci/protocol/openid-connect/userinfo",
				HttpMethod.GET,
				new HttpEntity<String>(headers),
				String.class);
		
		log.info("Query Keycloak user: {}", response.getBody());
		
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(
				response.getBody(), 
				KeycloakUserDTO.class).toUserBean();
	}

	private UserBean createUser(UserBean user) throws IOException {
		
		log.info("Create user: {}", user);
		
		return userRepository.saveAndFlush(user);
	}
}
