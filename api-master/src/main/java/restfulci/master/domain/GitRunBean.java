package restfulci.master.domain;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

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
	
	public GitJobBean getJob() {
		return (GitJobBean)super.getJob();
	}

	public JobType getType() {
		return JobType.GIT;
	}
}
