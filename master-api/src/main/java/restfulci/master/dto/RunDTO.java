package restfulci.master.dto;

import java.io.IOException;
import java.util.Date;

import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import restfulci.shared.domain.FreestyleRunBean;
import restfulci.shared.domain.GitBranchRunBean;
import restfulci.shared.domain.GitCommitRunBean;
import restfulci.shared.domain.RunBean;

@Getter
@Setter
@ToString
public class RunDTO {

	@Size(max=128)
	private String branchName;
	
	@Size(min=41, max=41)
	private String commitSha;
	
	public RunBean toBean() throws IOException {
		
		if (branchName != null) {
			
			GitBranchRunBean runBean = new GitBranchRunBean();
			runBean.setTriggerAt(new Date());
			runBean.setBranchName(branchName);
			return runBean;
		}
		
		if (commitSha != null) {
			
			GitCommitRunBean runBean = new GitCommitRunBean();
			runBean.setTriggerAt(new Date());
			runBean.setCommitSha(commitSha);
			return runBean;
		}
		
		FreestyleRunBean runBean = new FreestyleRunBean();
		runBean.setTriggerAt(new Date());
		return runBean;
	}
}
