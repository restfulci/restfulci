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
@Table(name="parameter")
public class ParameterBean extends BaseEntity {

	@ToString.Exclude
	@JsonIgnore
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="pipeline_id")
	private PipelineBean pipeline;
	
	@NotNull
	@Size(min=2, max=32)
	@Pattern(regexp="^[A-Z_][A-Z0-9_]*$")
	@Column(name="name", updatable=false)
	private String name;
}
