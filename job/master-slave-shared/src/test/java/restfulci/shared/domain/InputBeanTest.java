package restfulci.shared.domain;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InputBeanTest {
	
	private FreestyleJobBean job;
	
	@BeforeEach
	public void setUp() {
		
		job = new FreestyleJobBean();
		job.setId(123);
		job.setName("job");
		job.setDockerImage("busybox:1.31");
		job.setCommand(new String[] {"echo", "0"});
		
		ParameterBean parameter = new ParameterBean();
		parameter.setId(4);
		parameter.setName("INCLUDE");
		parameter.setChoices(new String[]{"qualified"});
		job.addParameter(parameter);
	}
	
	@Test
	public void testValidInput() throws Exception {
		
		FreestyleRunBean run = new FreestyleRunBean();
		run.setJob(job);
		
		InputBean input = new InputBean();
		input.setName("INCLUDE");
		input.setValue("qualified");
		run.addInput(input);
		
		run.validateInput();
	}
	
	@Test
	public void testInvalidInputName() throws Exception {
		
		FreestyleRunBean run = new FreestyleRunBean();
		run.setJob(job);
		
		InputBean input = new InputBean();
		input.setName("EXCLUDE");
		input.setValue("qualified");
		run.addInput(input);
		
		Assertions.assertThrows(IOException.class, () -> {
			run.validateInput();
		});
	}
	
	@Test
	public void testInvalidInputValue() throws Exception {
		
		FreestyleRunBean run = new FreestyleRunBean();
		run.setJob(job);
		
		InputBean input = new InputBean();
		input.setName("INCLUDE");
		input.setValue("not-qualified");
		run.addInput(input);
		
		Assertions.assertThrows(IOException.class, () -> {
			run.validateInput();
		});
	}
}
