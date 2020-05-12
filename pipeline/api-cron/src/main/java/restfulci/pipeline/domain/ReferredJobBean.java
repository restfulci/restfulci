package restfulci.pipeline.domain;

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
@Table(name="referred_job")
public class ReferredJobBean extends BaseEntity {

	@ToString.Exclude
	@JsonIgnore
	@NotNull
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="pipeline_id")
	private PipelineBean pipeline;
	
	@NotNull
	@Column(name="original_job_id")
	private Integer originalJobId;
}
