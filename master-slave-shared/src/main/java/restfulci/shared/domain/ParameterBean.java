package restfulci.shared.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name="parameter")
public class ParameterBean extends BaseEntity {
	
	@NotNull
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="job_id")
	@JsonIgnore
	private JobBean job;

	@NotNull
	@Column(name="name", updatable=false)
	private String name;
	
	/*
	 * Parameter always have type `string`, because that's the only
	 * supporting type for environment variables.
	 */
	@JsonInclude(Include.NON_NULL)
	@Column(name="default_value")
	private String defaultValue;
	
	@JsonInclude(Include.NON_EMPTY)
	@Type(type="string-array")
    @Column(name="choices", columnDefinition="text[]")
	private String[] choices;
}
