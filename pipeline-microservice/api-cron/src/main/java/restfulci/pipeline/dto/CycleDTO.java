package restfulci.pipeline.dto;

import java.io.IOException;
import java.util.HashMap;

import restfulci.pipeline.domain.CycleBean;
import restfulci.pipeline.domain.InputBean;

public class CycleDTO extends HashMap<String, String> {

	private static final long serialVersionUID = 1L;

	public CycleBean toCycleBean() throws IOException {

		CycleBean cycleBean = new CycleBean();

		for (String key : keySet()) {
			InputBean input = new InputBean();
			input.setCycle(cycleBean);
			input.setName(key);
			input.setValue(get(key));
			
			cycleBean.addInput(input);
		}

		/*
		 * TODO:
		 * Validate RunBean and InputBean, and raise associated exceptions.
		 * https://www.baeldung.com/javax-validation
		 */
		
		return cycleBean;
	}
}
