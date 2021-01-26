package restfulci.job.slave.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import restfulci.job.shared.domain.RunBean;

@Getter
@Setter
@ToString
public class RunCommandDTO {

	private Integer exitCode;
	
	private String runOutputObjectReferral;
	
	public void updateRunBean(RunBean run) {
		run.setExitCode(exitCode);
		run.setRunOutputObjectReferral(runOutputObjectReferral);
	}
}
