package restfulci.shared.domain;

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
@Table(name="input")
public class InputBean extends BaseEntity {
	
	@NotNull
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="run_id")
	@JsonIgnore
	private RunBean run;

	/*
	 * We refer to `name`, rather than `ParameterBean`, because the parameter
	 * set of a job may be changing. Runs are existing fact of historical 
	 * parameters.
	 */
	@NotNull
	@Size(min=2, max=32)
	@Pattern(regexp="^[A-Z_][A-Z0-9_]*$")
	@Column(name="name", updatable=false)
	private String name;
	
	@Column(name="value", updatable=false)
	private String value; 
}
