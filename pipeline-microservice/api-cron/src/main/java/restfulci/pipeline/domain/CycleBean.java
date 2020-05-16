package restfulci.pipeline.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
	
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd hh:mm:ss")
	@Column(name="trigger_at", updatable=false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date triggerAt;
	
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
