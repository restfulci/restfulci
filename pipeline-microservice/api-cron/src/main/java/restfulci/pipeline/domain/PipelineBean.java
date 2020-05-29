package restfulci.pipeline.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name="pipeline")
public class PipelineBean extends BaseEntity {

	@NotNull
	@Size(min=2, max=32)
	@Column(name="name")
	private String name;
	
	@JsonInclude(Include.NON_EMPTY)
	@OneToMany(targetEntity=ReferredJobBean.class, fetch=FetchType.EAGER, cascade=CascadeType.ALL, mappedBy="pipeline")
	private Set<ReferredJobBean> referredJobs = new HashSet<ReferredJobBean>();
	
	public ReferredJobBean getReferredJob(Integer referredJobId) {
		
		for (ReferredJobBean referredJob : referredJobs) {
			if (referredJob.getId().equals(referredJobId)) {
				return referredJob;
			}
		}
		return null;
	}
	
	/*
	 * TODO:
	 * A pipeline should not have 2 referred jobs with the same `originalJobId`.
	 * Otherwise we cannot get the referred job id with a return pipeline JSON.
	 */
	public void addReferredJob(ReferredJobBean referredJob) {
		referredJobs.add(referredJob);
	}
}
