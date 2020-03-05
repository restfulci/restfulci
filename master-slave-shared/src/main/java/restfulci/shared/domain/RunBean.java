package restfulci.shared.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name="run")
@Inheritance(strategy=InheritanceType.JOINED)
public class RunBean extends BaseEntity {
	
	@NotNull
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="job_id")
	private JobBean job;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
	@NotNull
	@Column(name="trigger_at", updatable=false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date triggerAt;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
	@Column(name="complete_at", updatable=false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date completeAt;
	
	/*
	 * TODO:
	 * `getDuration()`
	 */
	
	public RunMessageBean toRunMessage() {
		
		RunMessageBean runMessage = new RunMessageBean();
		runMessage.setJobId(job.getId());
		runMessage.setRunId(id);
		return runMessage;
	}
}
