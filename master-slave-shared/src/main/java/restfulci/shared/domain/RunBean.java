package restfulci.shared.domain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
	
	@JsonInclude(Include.NON_EMPTY)
	@OneToMany(targetEntity=InputBean.class, fetch=FetchType.LAZY, cascade=CascadeType.ALL, mappedBy="run")
	private List<InputBean> inputs = new ArrayList<InputBean>();

	public void addInput(InputBean input) {
		inputs.add(input);
	}
	
	@NotNull
	@Column(name="phase_shortname")
	@Convert(converter = RunPhaseConventer.class)
	private RunPhase phase;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
	@NotNull
	@Column(name="trigger_at", updatable=false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date triggerAt;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
	@Column(name="complete_at", updatable=true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date completeAt;
	
	@Column(name="exit_code", updatable=true)
	private Integer exitCode;
	
	@JsonIgnore
	@Column(name="run_output_object_referral", updatable=true)
	private String runOutputObjectReferral;
	
	/*
	 * TODO:
	 * Looks like we can have only one @OneToMany FetchType.EAGER throughout
	 * the code base (not a single domain class). Otherwise we'll face error:
	 * > Factory method 'entityManagerFactory' threw exception; nested exception 
	 * > is javax.persistence.PersistenceException: [PersistenceUnit: default] 
	 * > Unable to build Hibernate SessionFactory; nested exception is 
	 * > org.hibernate.loader.MultipleBagFetchException: cannot simultaneously 
	 * > fetch multiple bags: [restfulci.shared.domain.JobBean.parameters, 
	 * > restfulci.shared.domain.RunBean.runResults, restfulci.shared.domain.RunBean.inputs]
	 */
	@OneToMany(targetEntity=RunResultBean.class, fetch=FetchType.EAGER, cascade=CascadeType.ALL, mappedBy="run")
	private List<RunResultBean> runResults = new ArrayList<RunResultBean>();
	
	/*
	 * TODO:
	 * `getDuration()`
	 */
	
	public void validateInput() throws IOException {
		
		for (InputBean input : inputs) {
			ParameterBean parameter = job.getParameter(input.getName());
			
			if (parameter == null) {
				throw new IOException("Input "+input.getName()+" is not in the associated job's parameter list");
			}
			
			if (parameter.getChoices() == null) {
				continue;
			}
			else {
				if (!Arrays.asList(parameter.getChoices()).contains(input.getValue())) {
					throw new IOException("Input "+input.getName()+" has invalid value "+input.getValue());
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
