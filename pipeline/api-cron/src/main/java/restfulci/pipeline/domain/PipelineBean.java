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
	private Set<ReferredJobBean> pipelines = new HashSet<ReferredJobBean>();
}
