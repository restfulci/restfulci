package restfulci.job.shared.domain;

import java.io.IOException;
import java.time.Duration;
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
import restfulci.job.shared.exception.RunDataException;

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
	
	@NotNull
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="user_id")
	private UserBean user;
	
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
	
	public void validateInput() throws IOException {
		
		for (InputBean input : inputs) {
			ParameterBean parameter = job.getParameter(input.getName());
			
			/*
			 * TODO:
			 * We should probably silently swallow this error. This match what Spring default/Java validation API 
			 * do if the API data include an attribute they don't know.
			 */
			if (parameter == null) {
				throw new RunDataException("Input "+input.getName()+" is not in the associated job's parameter list");
			}
			
			if (parameter.getChoices() == null) {
				continue;
			}
			else {
				if (!Arrays.asList(parameter.getChoices()).contains(input.getValue())) {
					throw new RunDataException("Input "+input.getName()+" has invalid value "+input.getValue());
				}
			}
		}
	}
	
	public void fillInDefaultInput() throws RunDataException {
		
		for (ParameterBean parameter : job.getParameters()) {
			if (getInput(parameter.getName()) == null) {
				if (parameter.getDefaultValue() != null) {
					InputBean input = new InputBean();
					input.setName(parameter.getName());
					input.setValue(parameter.getDefaultValue());
					addInput(input);
				}
				else {
					throw new RunDataException("Missing input for "+parameter.getName());
				}
			}
		}
	}
	
	@NotNull
	@Column(name="status_shortname")
	@Convert(converter=RunStatusConventer.class)
	private RunStatus status = RunStatus.IN_PROGRESS;
	
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd hh:mm:ss")
	@NotNull
	@Column(name="trigger_at", updatable=false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date triggerAt = new Date();
	
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd hh:mm:ss")
	@Column(name="complete_at", updatable=true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date completeAt;
	
	public Long getDurationInSecond() {
		if (completeAt != null) {
			return Duration.between(triggerAt.toInstant(), completeAt.toInstant()).getSeconds();
		}
		else {
			return null;
		}
	}
	
	@Column(name="exit_code", updatable=true)
	private Integer exitCode;
	
	@JsonIgnore
	@Column(name="run_output_object_referral", updatable=true)
	private String runOutputObjectReferral;
	
	@JsonIgnore
	public String getDefaultRunOutputObjectReferral() {
		if (runOutputObjectReferral != null) {
			return runOutputObjectReferral;
		}
		if (id != null) {
			return id.toString();
		}
		return null;
	}
	
	@OneToMany(targetEntity=RunResultBean.class, fetch=FetchType.EAGER, cascade=CascadeType.ALL, mappedBy="run")
	private Set<RunResultBean> runResults = new HashSet<RunResultBean>();
	
	public abstract JobType getType();
	
	public RunMessageBean toRunMessage() {
		
		RunMessageBean runMessage = new RunMessageBean();
		runMessage.setJobId(job.getId());
		runMessage.setRunId(id);
		return runMessage;
	}
}
