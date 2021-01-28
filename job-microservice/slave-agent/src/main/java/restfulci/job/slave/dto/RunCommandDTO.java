package restfulci.job.slave.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import restfulci.job.shared.domain.RunBean;
import restfulci.job.shared.domain.RunStatus;

@Getter
@Setter
@ToString
public class RunCommandDTO {
	
	private RunStatus status;

	private Integer exitCode;
	
	private String runOutputObjectReferral;
	
	public void updateRunBean(RunBean run) {
		run.setStatus(status);
		run.setExitCode(exitCode);
		run.setRunOutputObjectReferral(runOutputObjectReferral);
	}
}
