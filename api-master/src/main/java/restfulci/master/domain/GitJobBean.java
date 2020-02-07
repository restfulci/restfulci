package restfulci.master.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@Table(name="git_job")
public class GitJobBean extends JobBean {

	/*
	 * TODO:
	 * Any reason to have an OneToMany for different remotes, as the remote
	 * is not necessarily to be called "origin"?
	 */
	@NotNull
	@Size(max=128)
	@Column(name="remote_origin")
	private String remoteOrigin;
	
	@NotNull
	@Size(max=128)
	@Column(name="config_filepath")
	private String configFilepath;
	
	public JobType getType() {
		return JobType.GIT;
	}
	
	public interface ConfigPlaceholder {
		String get(GitRunBean branchName);
	}
	
	@Transient
	@Getter(AccessLevel.NONE)
	@ToString.Exclude
	@JsonIgnore
	private ConfigPlaceholder configPlaceholder;
	
	@JsonIgnore
	public String getConfig(GitRunBean gitRun) {
		return configPlaceholder.get(gitRun);
	}
}
