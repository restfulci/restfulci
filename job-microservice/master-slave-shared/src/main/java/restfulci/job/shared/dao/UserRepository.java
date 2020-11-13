package restfulci.job.shared.dao;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

import restfulci.job.shared.domain.UserBean;

public interface UserRepository extends PagingAndSortingRepository<UserBean, Integer> {

	public List<UserBean> findByAuthId(String authId);
	public UserBean saveAndFlush(UserBean job);
	public void delete(UserBean user);
}
