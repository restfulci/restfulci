package restfulci.slave.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import restfulci.shared.domain.DockerRunCmdResultBean;

public class DockerRunServiceTest {

	@Test
	public void testFreestyleDockerRun() throws Exception {
		
		DockerRunService service = new DockerRunService();
		
		List<String> command = new ArrayList<String>();
		command.add("sh");
		command.add("-c");
		command.add("echo \"Hello world\"");
		DockerRunCmdResultBean result = service.runCommand("busybox", command);
		
		assertEquals(result.getExitCode(), 0);
		assertEquals(result.getOutput(), "Hello world\n");
	}
}
