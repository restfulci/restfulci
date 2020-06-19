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
				
				/*
				 * We should possibly define a map between data payload to 
				 * input/environment variables, so our job can basically accept
				 * payload in any format. And instead of saving the input key/value
				 * pairs, we just save the entire payload as input.
				 * 
				 * Then either the payload format (as the interface) need to be 
				 * defined outside of the config YAML -- same as currently we save 
				 * the parameters. It is very hard as it is more complicated than
				 * simply key/value pairs of input.
				 * Maybe https://json-schema.org/ works? Or we can use XSD if our 
				 * payload is XML?
				 * 
				 * Or we define the parameters in the YAML config. It is hard, as:
				 * (1) The format can't be completely define in YAML. As we need a
				 * fixed schema to know branchName/commitSha before we can ever 
				 * reach the YAML. 
				 * (2) It may work as a RESTful API (since potentially you can have
				 * different input schema for different commitSha which loads different
				 * config YAML). But if it has an UI, it is very hard to generate the
				 * input form (as you need to first pass the commitSha (as part of the 
				 * input) and then generate the input form).
				 */
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
