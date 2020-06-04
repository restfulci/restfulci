package restfulci.job.shared.domain;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import restfulci.job.shared.domain.exception.RunInputException;

@Getter
@Setter
@ToString
@Entity
@Table(name="run")
@Inheritance(strategy=InheritanceType.JOINED)
public abstract class RunBean extends BaseEntity {
	
	@NotNull
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="job_id")
	private JobBean job;
	
	/*
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
	@OneToMany(targetEntity=InputBean.class, fetch=FetchType.EAGER, cascade=CascadeType.ALL, mappedBy="run")
	private Set<InputBean> inputs = new HashSet<InputBean>();

	public void addInput(InputBean input) {
		inputs.add(input);
	}
	
	private InputBean getInput(String name) {
		/*
		 * TODO:
		 * A lazy evaluated lookup table.
		 */
		for (InputBean input : inputs) {
			if (input.getName().equals(name)) {
				return input;
			}
		}
		return null;
	}
	
	@NotNull
	@Column(name="status_shortname")
	@Convert(converter=RunStatusConventer.class)
	private RunStatus status;
	
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd hh:mm:ss")
	@NotNull
	@Column(name="trigger_at", updatable=false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date triggerAt;
	
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd hh:mm:ss")
	@Column(name="complete_at", updatable=true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date completeAt;
	
	@Column(name="exit_code", updatable=true)
	private Integer exitCode;
	
	@JsonIgnore
	@Column(name="run_output_object_referral", updatable=true)
	private String runOutputObjectReferral;
	
	@OneToMany(targetEntity=RunResultBean.class, fetch=FetchType.EAGER, cascade=CascadeType.ALL, mappedBy="run")
	private Set<RunResultBean> runResults = new HashSet<RunResultBean>();
	
	/*
	 * TODO:
	 * `getDuration()`
	 */
	
	public void validateInput() throws IOException {
		
		for (InputBean input : inputs) {
			ParameterBean parameter = job.getParameter(input.getName());
			
			if (parameter == null) {
				throw new RunInputException("Input "+input.getName()+" is not in the associated job's parameter list");
			}
			
			if (parameter.getChoices() == null) {
				continue;
			}
			else {
				if (!Arrays.asList(parameter.getChoices()).contains(input.getValue())) {
					throw new RunInputException("Input "+input.getName()+" has invalid value "+input.getValue());
				}
			}
		}
	}
	
	public void fillInDefaultInput() throws RunInputException {
		
		for (ParameterBean parameter : job.getParameters()) {
			if (getInput(parameter.getName()) == null) {
				if (parameter.getDefaultValue() != null) {
					InputBean input = new InputBean();
					input.setName(parameter.getName());
					input.setValue(parameter.getDefaultValue());
					addInput(input);
				}
				else {
					throw new RunInputException("Missing input for "+parameter.getName());
				}
			}
		}
	}
	
	public RunMessageBean toRunMessage() {
		
		RunMessageBean runMessage = new RunMessageBean();
		runMessage.setJobId(job.getId());
		runMessage.setRunId(id);
		return runMessage;
	}
}
