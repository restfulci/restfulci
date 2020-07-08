package restfulci.pipeline.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name="parameter_map")
public class ParameterMapBean extends BaseEntity {

	@ToString.Exclude
	@JsonIgnore
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="referred_job_id")
	private ReferredJobBean referredJob;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="parameter_id")
	private ParameterBean parameter;
	
	@NotNull
	@Size(min=2, max=32)
	@Pattern(regexp="^[A-Z_][A-Z0-9_]*$")
	@Column(name="remote_name", updatable=false)
	private String remoteName;
	
	@NotNull
	@Column(name="optional")
	private Boolean optional;
}
