package restfulci.master.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name="job")
@Inheritance(strategy=InheritanceType.JOINED)
public abstract class JobBean {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id", updatable=false)
	private Integer id;
	
	@NotNull
	@Size(min=2, max=32)
	@Column(name="name")
	private String name;
	
	@ToString.Exclude
	@JsonIgnore
	@OneToMany(targetEntity=RunBean.class, fetch=FetchType.LAZY, cascade=CascadeType.ALL, mappedBy="job")
	private List<RunBean> runs = new ArrayList<RunBean>();
	
	public abstract JobType getType();
}
