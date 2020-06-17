package restfulci.job.master.dto;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import restfulci.job.shared.domain.FreestyleRunBean;
import restfulci.job.shared.domain.GitBranchRunBean;
import restfulci.job.shared.domain.GitCommitRunBean;
import restfulci.job.shared.domain.InputBean;
import restfulci.job.shared.domain.RunBean;

public class RunDTO extends HashMap<String, String> {

	private static final long serialVersionUID = 1L;

	public RunBean toRunBean() throws IOException {

		RunBean runBean;

		if (containsKey("branchName")) {
			
			GitBranchRunBean gitBranchRunBean = new GitBranchRunBean();
			gitBranchRunBean.setBranchName(get("branchName"));
			runBean = gitBranchRunBean;
		} 
		else if (containsKey("commitSha")) {
			
			GitCommitRunBean gitCommitRunBean = new GitCommitRunBean();
			gitCommitRunBean.setCommitSha(get("commitSha"));
			runBean = gitCommitRunBean;	
		} 
		else {
			
			runBean = new FreestyleRunBean();
		}

		for (String key : keySet()) {
			if (!key.equals("branchName") && !key.equals("commitSha")) {
				
				InputBean input = new InputBean();
				input.setRun(runBean);
				input.setName(key);
				input.setValue(get(key));
				
				runBean.addInput(input);
			}
		}
		
		/*
		 * TODO:
		 * Validate RunBean and InputBean, and raise associated exceptions.
		 * https://www.baeldung.com/javax-validation
		 */
		
		return runBean;
	}
}
