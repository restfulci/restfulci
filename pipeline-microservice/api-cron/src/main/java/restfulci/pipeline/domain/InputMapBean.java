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
@Table(name="input_map")
public class InputMapBean extends BaseEntity {

	@ToString.Exclude
	@JsonIgnore
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="referred_run_id")
	private ReferredRunBean referredRun;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="input_id")
	private InputBean input;
	
	@NotNull
	@Size(min=2, max=32)
	@Pattern(regexp="^[A-Z_][A-Z0-9_]*$")
	@Column(name="remote_name", updatable=false)
	private String remoteName;
}
