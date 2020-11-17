package restfulci.job.master.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import restfulci.job.shared.domain.UserBean;

@Getter
@Setter
@ToString
public class KeycloakUserDTO {

	private String sub;
	
	private String preferred_username;
	
	public UserBean toUserBean() {
		UserBean userBean = new UserBean();
		userBean.setAuthId(sub);
		userBean.setUsername(preferred_username);
		return userBean;
	}
}
