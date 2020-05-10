package restfulci.master.dto;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import restfulci.shared.domain.FreestyleRunBean;
import restfulci.shared.domain.GitBranchRunBean;
import restfulci.shared.domain.GitCommitRunBean;
import restfulci.shared.domain.InputBean;
import restfulci.shared.domain.RunBean;
import restfulci.shared.domain.RunPhase;

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

		runBean.setPhase(RunPhase.IN_PROGRESS);
		runBean.setTriggerAt(new Date());
		
		/*
		 * TODO:
		 * Validate RunBean and InputBean, and raise associated exceptions.
		 * https://www.baeldung.com/javax-validation
		 */
		
		return runBean;
	}
}
