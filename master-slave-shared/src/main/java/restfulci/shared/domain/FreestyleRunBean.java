package restfulci.shared.domain;

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
@Table(name="freestyle_run")
@Inheritance(strategy=InheritanceType.JOINED)
public class FreestyleRunBean extends RunBean {
	
	public FreestyleJobBean getJob() {
		return (FreestyleJobBean)super.getJob();
	}

	public JobType getType() {
		return JobType.FREESTYLE;
	}
}
