package restfulci.shared.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name="git_run")
@Inheritance(strategy=InheritanceType.JOINED)
public abstract class GitRunBean extends RunBean {
	
	@JsonIgnore
	@Column(name="run_configuration_object_referral", updatable=true)
	private String runConfigurationObjectReferral;
	
	public GitJobBean getJob() {
		return (GitJobBean)super.getJob();
	}

	public JobType getType() {
		return JobType.GIT;
	}
}
