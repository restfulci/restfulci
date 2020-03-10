package restfulci.shared.domain;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import com.vladmihalcea.hibernate.type.array.IntArrayType;
import com.vladmihalcea.hibernate.type.array.StringArrayType;

import lombok.Getter;
import lombok.Setter;

@TypeDefs({
	@TypeDef(
		name="string-array",
		typeClass = StringArrayType.class
	),
	@TypeDef(
		name="int-array",
		typeClass=IntArrayType.class
	)
})
@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {
 
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id", updatable=false)
	protected Integer id;
}
