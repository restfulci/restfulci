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
	@Column(name="status_shortname")
	@Convert(converter=ReferredRunStatusConventer.class)
	private ReferredRunStatus status;
	
	/*
	 * Can't use subclasses, because ReferredRun doesn't know its status
	 * at the beginning.
	 */
	@Column(name="error_message", updatable=true)
	private String errorMessage;
	
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
	
	public void updateFromRemoteRun(RemoteRunBean remoteRun) {
		originalRunId = remoteRun.getId();
		status = ReferredRunStatus.valueOf(remoteRun.getStatus());
		exitCode = remoteRun.getExitCode();
	}
}
