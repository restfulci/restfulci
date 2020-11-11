package restfulci.job.master.service;

import java.io.IOException;

import restfulci.job.shared.domain.UserBean;

public interface UserService {
	
	public UserBean getUserByAuthId(String authId, String token) throws IOException;
}
