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
@Table(name="git_commit_run")
public class GitCommitRunBean extends GitRunBean {
	
	@NotNull
	@Size(min=41, max=41)
	@Column(name="commit_sha")
	private String commitSha;
}
