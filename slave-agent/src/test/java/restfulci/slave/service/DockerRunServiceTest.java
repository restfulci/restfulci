package restfulci.slave.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import restfulci.shared.domain.DockerRunCmdResultBean;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class DockerRunServiceTest {
	
	@Autowired DockerRunService service;

	@Test
	public void testRunCommand() throws Exception {
		
		List<String> command = new ArrayList<String>();
		command.add("sh");
		command.add("-c");
		command.add("echo \"Hello world\"");
		DockerRunCmdResultBean result = service.runCommand("busybox", command);
		
		assertEquals(result.getExitCode(), 0);
		assertEquals(result.getOutput(), "Hello world\n");
	}
}
