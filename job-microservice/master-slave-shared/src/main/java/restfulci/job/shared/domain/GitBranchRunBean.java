package restfulci.job.shared.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name="git_branch_run")
public class GitBranchRunBean extends GitRunBean {
	
	@NotNull
	@Size(max=128)
	@Column(name="branch_name")
	private String branchName;
	
	/*
	 * TODO:
	 * General commit SHA.
	 */
}
