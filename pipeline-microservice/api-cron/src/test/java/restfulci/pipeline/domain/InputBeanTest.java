package restfulci.pipeline.domain;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InputBeanTest {
	
	private PipelineBean pipeline;
	
	@BeforeEach
	public void setUp() {
		
		pipeline = new PipelineBean();
		pipeline.setId(123);
		
		ParameterBean parameter = new ParameterBean();
		parameter.setId(4);
		parameter.setName("INCLUDE");
		parameter.setChoices(new String[]{"qualified"});
		pipeline.addParameter(parameter);
	}
	
	@Test
	public void testValidInput() throws Exception {
		
		CycleBean cycle = new CycleBean();
		cycle.setPipeline(pipeline);
		
		InputBean input = new InputBean();
		input.setName("INCLUDE");
		input.setValue("qualified");
		cycle.addInput(input);
		
		cycle.validateInput();
	}
	
	@Test
	public void testInvalidInputName() throws Exception {
		
		CycleBean cycle = new CycleBean();
		cycle.setPipeline(pipeline);
		
		InputBean input = new InputBean();
		input.setName("EXCLUDE");
		input.setValue("qualified");
		cycle.addInput(input);
		
		assertThrows(IOException.class, () -> {
			cycle.validateInput();
		});
	}
	
	@Test
	public void testInvalidInputValue() throws Exception {
		
		CycleBean cycle = new CycleBean();
		cycle.setPipeline(pipeline);
		
		InputBean input = new InputBean();
		input.setName("INCLUDE");
		input.setValue("not-qualified");
		cycle.addInput(input);
		
		assertThrows(IOException.class, () -> {
			cycle.validateInput();
		});
	}
}
