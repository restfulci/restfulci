package restfulci.job.master.service;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import restfulci.job.master.dto.KeycloakUserDTO;
import restfulci.job.shared.dao.UserRepository;
import restfulci.job.shared.domain.UserBean;

@Service
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {
	
	@Autowired private UserRepository userRepository;

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
			UserBean user = getUserFromToken(token);
			return createUser(user);
		}
	}
	
	private UserBean getUserFromToken(String token) throws IOException {
		
		token = token.replaceFirst("^Bearer ", "");
		String withoutSignature = token.substring(0, token.lastIndexOf('.') + 1);
		Jwt<Header, Claims> untrusted = Jwts.parser().parseClaimsJwt(withoutSignature);
		
		KeycloakUserDTO userDTO = new KeycloakUserDTO();
		userDTO.setSub(untrusted.getBody().get("sub").toString());
		userDTO.setPreferred_username(untrusted.getBody().get("preferred_username").toString());
		log.info("Get user authId and username from token:"+userDTO);
		
		return userDTO.toUserBean();
	}

	private UserBean createUser(UserBean user) throws IOException {
		
		log.info("Create user: {}", user);
		
		return userRepository.saveAndFlush(user);
	}
}
