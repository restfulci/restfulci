package restfulci.master.dto;

import java.io.IOException;
import java.util.Date;

import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import restfulci.master.domain.GitBranchRunBean;
import restfulci.master.domain.GitCommitRunBean;
import restfulci.master.domain.RunBean;

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
		
		throw new IOException();
		/* 
		 * TODO:
		 * Should input a type, rather than completely rely on input content negotiation?
		 */
	}
}
