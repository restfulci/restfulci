package restfulci.job.shared.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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
@Table(name="job")
@Inheritance(strategy=InheritanceType.JOINED)
public abstract class JobBean extends BaseEntity {
	
	@NotNull
	@Size(min=2, max=32)
	@Column(name="name")
	private String name;
	
	@ToString.Exclude
	@JsonIgnore
	@OneToMany(targetEntity=RunBean.class, fetch=FetchType.LAZY, cascade=CascadeType.ALL, mappedBy="job")
	private List<RunBean> runs = new ArrayList<RunBean>();
	
	/*
	 * Potentially we don't need this. We can just ask the "run"
	 * to pass wildcard input parameters (as data dictionary), and 
	 * pass everything as environmental variables into the container. 
	 * By doing so, we can even have the same job to have different 
	 * kind of input parameters for different version (commit SHA)
	 * of the job.
	 * 
	 * We don't want to do that, because:
	 * (1) We want some validation that the data passed in.
	 * (2) We want to be able to generate an input form in the UI.
	 * 
	 * --
	 * 
	 * Use `Set` so there can be multiple `FetchType.EAGER`. If use `List`, we'll 
	 * face error:
	 * > Factory method 'entityManagerFactory' threw exception; nested exception 
	 * > is javax.persistence.PersistenceException: [PersistenceUnit: default] 
	 * > Unable to build Hibernate SessionFactory; nested exception is 
	 * > org.hibernate.loader.MultipleBagFetchException: cannot simultaneously 
	 * > fetch multiple bags: [restfulci.shared.domain.JobBean.parameters, 
	 * > restfulci.shared.domain.RunBean.runResults, restfulci.shared.domain.RunBean.inputs]
	 * 
	 * One problem is using sets you won't eliminate the underlying Cartesian Product.
	 * It is fine for us, as we typically don't have a lot of parameters/inputs/results/...
	 * https://stackoverflow.com/a/4335514/2467072
	 */
	@JsonInclude(Include.NON_EMPTY)
	@OneToMany(targetEntity=ParameterBean.class, fetch=FetchType.EAGER, cascade=CascadeType.ALL, mappedBy="job")
	private Set<ParameterBean> parameters = new HashSet<ParameterBean>();
	
	public void addParameter(ParameterBean parameter) {
		parameters.add(parameter);
	}
	
	ParameterBean getParameter(String name) {
		/*
		 * TODO:
		 * A lazy evaluated lookup table.
		 */
		for (ParameterBean parameter : parameters) {
			if (parameter.getName().equals(name)) {
				return parameter;
			}
		}
		return null;
	}
	
	public abstract JobType getType();
}
