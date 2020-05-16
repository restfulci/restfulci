package restfulci.pipeline.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

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
@Table(name="referred_run")
public class ReferredRunBean extends BaseEntity {

	@ToString.Exclude
	@JsonIgnore
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="cycle_id")
	private CycleBean cycle;
	
	@NotNull
	@Column(name="original_job_id")
	private Integer originalJobId;
	
	@Column(name="original_run_id")
	private Integer originalRunId;
	
	@NotNull
	@Column(name="phase_shortname")
	@Convert(converter=ReferredRunPhaseConventer.class)
	private ReferredRunPhase phase;
	
	@Column(name="exit_code", updatable=true)
	private Integer exitCode;
	
	@JsonInclude(Include.NON_EMPTY)
	@ManyToMany
	@JoinTable(
		name="referred_run_dependency", 
		joinColumns=@JoinColumn(name="downstream_referred_run_id"), 
		inverseJoinColumns=@JoinColumn(name="upstream_referred_run_id"))
	private Set<ReferredRunBean> referredUpstreamRuns = new HashSet<ReferredRunBean>();
	
	public void addReferredUpstreamRun(ReferredRunBean referredRun) {
		referredUpstreamRuns.add(referredRun);
	}
}
