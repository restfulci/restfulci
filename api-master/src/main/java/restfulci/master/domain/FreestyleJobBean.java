package restfulci.master.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="freestyle_job")
public class FreestyleJobBean extends JobBean {

	/*
	 * TODO:
	 * Any reason to have an OneToMany for different remotes, as the remote
	 * is not necessarily to be called "origin"?
	 */
	@NotNull
	@Column(name="script")
	private String script;
	
	public String getType() {
		return "freestyle";
	}
}
