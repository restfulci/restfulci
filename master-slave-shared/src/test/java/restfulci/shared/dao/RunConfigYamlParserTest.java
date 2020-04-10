package restfulci.shared.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import restfulci.shared.domain.RunConfigBean;

public class RunConfigYamlParserTest {

	@Test
	public void testParse() throws IOException {
		
		File exampleYamlFile = new File(getClass().getClassLoader().getResource("example-restfulci.yml").getFile());
		String yamlContent = String.join("\n", Files.readAllLines(exampleYamlFile.toPath()));
		
		RunConfigBean runConfig = RunConfigYamlParser.parse(yamlContent);
		
		assertEquals(runConfig.getVersion(), "1.0");
		assertEquals(runConfig.getEnvironment().getBuild().getContext(), "./path/to/subproject");
		assertEquals(runConfig.getEnvironment().getBuild().getDockerfile(), "./Dockerfile");
		assertEquals(runConfig.getCommand().size(), 3);
		assertEquals(runConfig.getCommand().get(0), "bash");
		assertEquals(runConfig.getCommand().get(1), "-c");
		assertEquals(runConfig.getCommand().get(2), "\"echo 0\"");
		assertEquals(runConfig.getResults().size(), 2);
		assertEquals(runConfig.getResults().get(0).getType(), "junit");
		assertEquals(runConfig.getResults().get(0).getPath(), "target/surefile-reports");
		assertEquals(runConfig.getResults().get(1).getType(), "junit");
		assertEquals(runConfig.getResults().get(1).getPath(), "target/failsafe-reports");
		
		assertEquals(
				runConfig.getDockerfile(Paths.get("/")).getCanonicalPath(), 
				"/path/to/subproject/Dockerfile");
	}
}
