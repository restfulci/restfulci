package restfulci.pipeline.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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
@Table(name="referred_job")
public class ReferredJobBean extends BaseEntity {

	@ToString.Exclude
	@JsonIgnore
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="pipeline_id")
	private PipelineBean pipeline;
	
	@NotNull
	@Column(name="original_job_id")
	private Integer originalJobId;
	
	@JsonInclude(Include.NON_EMPTY)
	@OneToMany(targetEntity=ParameterMapBean.class, fetch=FetchType.EAGER, cascade=CascadeType.ALL, mappedBy="referredJob")
	private Set<ParameterMapBean> parameterMaps = new HashSet<ParameterMapBean>();
	
	public void addParameterMap(ParameterMapBean parameterMap) {
		parameterMaps.add(parameterMap);
	}
	
	public ParameterMapBean getParameterMap(Integer parameterMapId) {
		for (ParameterMapBean parameterMap : parameterMaps) {
			if (parameterMap.getId().equals(parameterMapId)) {
				return parameterMap;
			}
		}
		return null;
	}
	
	public ParameterMapBean getParameterMap(String remoteName) {
		for (ParameterMapBean parameterMap : parameterMaps) {
			if (parameterMap.getRemoteName().equals(remoteName)) {
				return parameterMap;
			}
		}
		return null;
	}
	
	@JsonInclude(Include.NON_EMPTY)
	@ManyToMany
	@JoinTable(
		name="referred_job_dependency", 
		joinColumns=@JoinColumn(name="downstream_referred_job_id"), 
		inverseJoinColumns=@JoinColumn(name="upstream_referred_job_id"))
	private Set<ReferredJobBean> referredUpstreamJobs = new HashSet<ReferredJobBean>();
	
	public void addReferredUpstreamJob(ReferredJobBean referredJob) {
		referredUpstreamJobs.add(referredJob);
	}
}
