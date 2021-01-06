package restfulci.job.shared.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import restfulci.job.shared.dao.RunConfigYamlParser;
import restfulci.job.shared.domain.RunConfigBean;

public class RunConfigYamlParserTest {
	
	@Test
	public void testParseMinimalWithImage() throws IOException {
		
		File exampleYamlFile = new File(getClass().getClassLoader().getResource("restfulci-minimal-image.yml").getFile());
		String yamlContent = String.join("\n", Files.readAllLines(exampleYamlFile.toPath()));
		
		RunConfigBean runConfig = RunConfigYamlParser.parse(yamlContent);
		
		assertEquals(runConfig.getVersion(), "1.0");
		assertEquals(runConfig.getExecutor().getImage(), "busybox:1.31");
		assertEquals(runConfig.getExecutor().getEnvironment().size(), 0);
		assertEquals(runConfig.getSidecars().size(), 0);
		assertEquals(runConfig.getCommand().size(), 3);
		assertEquals(runConfig.getCommand().get(0), "bash");
		assertEquals(runConfig.getCommand().get(1), "-c");
		assertEquals(runConfig.getCommand().get(2), "\"echo 0\"");
		assertEquals(runConfig.getResults().size(), 0);
	}

	@Test
	public void testParseBuild() throws IOException {
		
		File exampleYamlFile = new File(getClass().getClassLoader().getResource("restfulci-from-build.yml").getFile());
		String yamlContent = String.join("\n", Files.readAllLines(exampleYamlFile.toPath()));
		
		RunConfigBean runConfig = RunConfigYamlParser.parse(yamlContent);
		
		assertEquals(runConfig.getVersion(), "1.0");
		assertEquals(runConfig.getExecutor().getBuild().getContext(), "./path/to/subproject");
		assertEquals(runConfig.getExecutor().getBuild().getDockerfile(), "./Dockerfile");
		assertEquals(runConfig.getExecutor().getEnvironment().size(), 0);
		assertEquals(runConfig.getSidecars().size(), 0);
		assertEquals(runConfig.getCommand().size(), 3);
		assertEquals(runConfig.getCommand().get(0), "bash");
		assertEquals(runConfig.getCommand().get(1), "-c");
		assertEquals(runConfig.getCommand().get(2), "\"echo 0\"");
		assertEquals(runConfig.getResults().size(), 0);
		
		assertEquals(
				runConfig.getBaseDir(Paths.get("/repo")).getCanonicalPath(), 
				"/repo/path/to/subproject");
		assertEquals(
				runConfig.getDockerfile(Paths.get("/repo")).getCanonicalPath(), 
				"/repo/path/to/subproject/Dockerfile");
	}
	
	@Test
	public void testParseWithEnvAndResult() throws IOException {
		
		File exampleYamlFile = new File(getClass().getClassLoader().getResource("restfulci-with-env-and-result.yml").getFile());
		String yamlContent = String.join("\n", Files.readAllLines(exampleYamlFile.toPath()));
		
		RunConfigBean runConfig = RunConfigYamlParser.parse(yamlContent);
		
		assertEquals(runConfig.getVersion(), "1.0");
		assertEquals(runConfig.getExecutor().getImage(), "busybox:1.31");
		assertEquals(runConfig.getExecutor().getEnvironment().size(), 1);
		assertEquals(runConfig.getExecutor().getEnvironment().get("FOO"), "bar");
		assertEquals(runConfig.getSidecars().size(), 0);
		assertEquals(runConfig.getCommand().size(), 3);
		assertEquals(runConfig.getCommand().get(0), "bash");
		assertEquals(runConfig.getCommand().get(1), "-c");
		assertEquals(runConfig.getCommand().get(2), "\"echo $FOO\"");
		assertEquals(runConfig.getResults().size(), 2);
		assertEquals(runConfig.getResults().get(0).getType(), "junit");
		assertEquals(runConfig.getResults().get(0).getPath(), "target/surefile-reports");
		assertEquals(runConfig.getResults().get(1).getType(), "junit");
		assertEquals(runConfig.getResults().get(1).getPath(), "target/failsafe-reports");
	}
	
	@Test
	public void testParseWithSidecars() throws IOException {
		
		File exampleYamlFile = new File(getClass().getClassLoader().getResource("restfulci-with-sidecars.yml").getFile());
		String yamlContent = String.join("\n", Files.readAllLines(exampleYamlFile.toPath()));
		
		RunConfigBean runConfig = RunConfigYamlParser.parse(yamlContent);
		
		assertEquals(runConfig.getVersion(), "1.0");
		assertEquals(runConfig.getExecutor().getImage(), "busybox:1.31");
		assertEquals(runConfig.getExecutor().getEnvironment().size(), 0);
		assertEquals(runConfig.getSidecars().size(), 2);
		assertEquals(runConfig.getSidecars().get(0).getName(), "lazybox");
		assertEquals(runConfig.getSidecars().get(0).getImage(), "busybox:1.31");
		assertEquals(runConfig.getSidecars().get(0).getEnvironment().size(), 0);
		assertEquals(runConfig.getSidecars().get(1).getName(), "sleepingbox");
		assertEquals(runConfig.getSidecars().get(1).getImage(), "busybox:1.31");
		assertEquals(runConfig.getSidecars().get(1).getEnvironment().size(), 1);
		assertEquals(runConfig.getSidecars().get(1).getEnvironment().get("FOO"), "bar");
		assertEquals(runConfig.getCommand().size(), 3);
		assertEquals(runConfig.getCommand().get(0), "bash");
		assertEquals(runConfig.getCommand().get(1), "-c");
		assertEquals(runConfig.getCommand().get(2), "\"ping -c 2 lazybox\"");
		assertEquals(runConfig.getResults().size(), 0);
	}
}
