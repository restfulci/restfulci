package restfulci.shared.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="freestyle_job")
public class FreestyleJobBean extends JobBean {

	@NotNull
	@Column(name="docker_image")
	private String dockerImage;
	
	@NotNull
	@Type(type="string-array")
    @Column(name="command", columnDefinition="text[]")
	private String[] command;
	
	public JobType getType() {
		return JobType.FREESTYLE;
	}
	
	/*
	 * TODO:
	 * Add input parameters
	 */
}
