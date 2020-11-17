package restfulci.job.shared.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name="auth_user")
public class UserBean extends BaseEntity {

	@NotNull
	@Column(name="auth_id", updatable=false)
	private String authId;

	@JsonInclude(Include.NON_NULL)
	@Column(name="username")
	private String username;
}
