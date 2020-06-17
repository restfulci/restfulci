package restfulci.pipeline.domain;

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
import restfulci.pipeline.exception.CycleDataException;

@Getter
@Setter
@ToString
@Entity
@Table(name="cycle")
public class CycleBean extends BaseEntity {

	@NotNull
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="pipeline_id")
	private PipelineBean pipeline;
	
	@JsonInclude(Include.NON_EMPTY)
	@OneToMany(targetEntity=InputBean.class, fetch=FetchType.EAGER, cascade=CascadeType.ALL, mappedBy="cycle")
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
			ParameterBean parameter = pipeline.getParameter(input.getName());
			
			/*
			 * TODO:
			 * We should probably silently swallow this error. This match what Spring default/Java validation API 
			 * do if the API data include an attribute they don't know.
			 */
			if (parameter == null) {
				throw new CycleDataException("Input "+input.getName()+" is not in the associated pipeline's parameter list");
			}
			
			if (parameter.getChoices() == null) {
				continue;
			}
			else {
				if (!Arrays.asList(parameter.getChoices()).contains(input.getValue())) {
					throw new CycleDataException("Input "+input.getName()+" has invalid value "+input.getValue());
				}
			}
		}
	}
	
	public void fillInDefaultInput() throws CycleDataException {
		
		for (ParameterBean parameter : pipeline.getParameters()) {
			if (getInput(parameter.getName()) == null) {
				if (parameter.getDefaultValue() != null) {
					InputBean input = new InputBean();
					input.setName(parameter.getName());
					input.setValue(parameter.getDefaultValue());
					addInput(input);
				}
				else {
					throw new CycleDataException("Missing input for "+parameter.getName());
				}
			}
		}
	}
	
	@NotNull
	@Column(name="status_shortname")
	@Convert(converter=CycleStatusConventer.class)
	private CycleStatus status = CycleStatus.IN_PROGRESS;
	
	@JsonIgnore
	@NotNull
	@Column(name="unfinalized_status_shortname")
	@Convert(converter=CycleStatusConventer.class)
	private CycleStatus unfinalizedStatus = CycleStatus.SUCCEED;
	
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd hh:mm:ss")
	@Column(name="trigger_at", updatable=false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date triggerAt = new Date();
	
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd hh:mm:ss")
	@Column(name="complete_at", updatable=true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date completeAt;
	
	@JsonInclude(Include.NON_EMPTY)
	@OneToMany(targetEntity=ReferredRunBean.class, fetch=FetchType.EAGER, cascade=CascadeType.ALL, mappedBy="cycle")
	private Set<ReferredRunBean> referredRuns = new HashSet<ReferredRunBean>();
	
	public ReferredRunBean getReferredRun(Integer referredRunId) {
		
		for (ReferredRunBean referredRun : referredRuns) {
			if (referredRun.getId().equals(referredRunId)) {
				return referredRun;
			}
		}
		return null;
	}
	
	public ReferredRunBean getReferredRunByOriginalJobId(Integer originalJobId) {
		
		for (ReferredRunBean referredRun : referredRuns) {
			if (referredRun.getOriginalJobId().equals(originalJobId)) {
				return referredRun;
			}
		}
		return null;
	}
	
	public void addReferredRun(ReferredRunBean referredRun) {
		referredRuns.add(referredRun);
	}
}
