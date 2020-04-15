package restfulci.shared.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name="run_result")
public class RunResultBean extends BaseEntity {
	
	@NotNull
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="run_id")
	private RunBean run;
	
	@NotNull
	@Column(name="type")
	private String type;
	
	@NotNull
	@Column(name="container_path")
	private String containerPath;
	
	@JsonIgnore
	@Column(name="object_referral", updatable=true)
	private String objectReferral;
	
}
