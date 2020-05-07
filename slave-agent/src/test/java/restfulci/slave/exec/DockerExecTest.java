package restfulci.slave.exec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import restfulci.shared.dao.MinioRepository;
import restfulci.shared.dao.RunRepository;
import restfulci.shared.domain.FreestyleRunBean;
import restfulci.shared.domain.RunBean;
import restfulci.shared.domain.RunConfigBean;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class DockerExecTest {
	
	@Autowired private DockerExec exec;
	
	@MockBean private RunRepository runRepository;
	@MockBean private MinioRepository minioRepository;
	
	@Test
	public void testPullImage() throws Exception {
		exec.pullImage("busybox");
		exec.pullImage("busybox:1.31");
		exec.pullImage("busybox:latest");
	}
	
	@Test
	public void testRunCommand() throws Exception {
		
		RunBean run = new FreestyleRunBean();
		
		exec.pullImage("busybox:1.31");
		exec.runCommandAndUpdateRunBean(
				run, 
				"busybox:1.31", 
				Arrays.asList(new String[]{"sh", "-c", "echo \"Hello world\""}), 
				new HashMap<String, String>(),
				new HashMap<RunConfigBean.RunConfigResultBean, File>());
		
		assertEquals(run.getExitCode(), 0);
		/*
		 * Cannot assert this, as the set logic is in `minioRepository`
		 * which is mocked here.
		 */
//		assertNotNull(run.getRunOutputObjectReferral());
		
		ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
		verify(minioRepository, times(1)).putRunOutputAndUpdateRunBean(eq(run), inputStreamCaptor.capture());
		assertEquals(IOUtils.toString(inputStreamCaptor.getValue(), StandardCharsets.UTF_8.name()), "Hello world\n");
	}
	
	@Test
	public void testRunCommandWithInput() throws Exception {
		
		RunBean run = new FreestyleRunBean();
		
		Map<String, String> inputs = new HashMap<String, String>();
		inputs.put("WORD", "customized input");
		
		exec.pullImage("busybox:1.31");
		exec.runCommandAndUpdateRunBean(
				run, 
				"busybox:1.31", 
				Arrays.asList(new String[]{"sh", "-c", "echo \"Hello $WORD\""}), 
				inputs,
				new HashMap<RunConfigBean.RunConfigResultBean, File>());
		
		assertEquals(run.getExitCode(), 0);
		/*
		 * Cannot assert this, as the set logic is in `minioRepository`
		 * which is mocked here.
		 */
//		assertNotNull(run.getRunOutputObjectReferral());
		
		ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
		verify(minioRepository, times(1)).putRunOutputAndUpdateRunBean(eq(run), inputStreamCaptor.capture());
		assertEquals(IOUtils.toString(inputStreamCaptor.getValue(), StandardCharsets.UTF_8.name()), "Hello customized input\n");
	}
	
	/*
	 * This test can pass locally but will fail CircleCI with error:
	 * > assertEquals(hostMountPoint.list().length, 1);
	 * > org.opentest4j.AssertionFailedError: expected: <0> but was: <1>
	 * 
	 * Should be CircleCI disables docker volume/mount.
	 */
	@Test
	@DisabledIfEnvironmentVariable(named="CI", matches="CircleCI")
	public void testRunCommandWithMount(@TempDir File tmpFolder) throws Exception {
		
		/*
		 * For Docker desktop on Mac, need to edit `Library/Group\ Containers/group.com.docker/settings.json`
		 * to add `/var/folders`. Otherwise error:
		 * > com.github.dockerjava.api.exception.InternalServerErrorException: Mounts denied: 
		 * > The path /var/folders/kp/fz7j3bln4m11rc197xrj4dvc0000gq/T/junit5099823135583804007/results
		 * > is not shared from OS X and is not known to Docker.
		 * > You can configure shared paths from Docker -> Preferences... -> File Sharing.
		 * > See https://docs.docker.com/docker-for-mac/osxfs/#namespaces for more info.
		 * 
		 * Notice that both `@TempDir` and `Files.createTempDirectory` 
		 * are using `/var/folders` to save the file.
		 */
		File hostMountPoint = new File(tmpFolder, "result");
		hostMountPoint.mkdir();
		
		RunConfigBean.RunConfigResultBean runConfigResult = new RunConfigBean.RunConfigResultBean();
		runConfigResult.setPath("/result");
		Map<RunConfigBean.RunConfigResultBean, File> mounts = new HashMap<RunConfigBean.RunConfigResultBean, File>();
		mounts.put(runConfigResult, hostMountPoint);
		
		RunBean run = new FreestyleRunBean();
		
		exec.pullImage("busybox:1.31");
		exec.runCommandAndUpdateRunBean(
				run, 
				"busybox:1.31", 
				/*
				 * No need to `mkdir /result`, as docker will create `/result`
				 * when there's a volume mount `/some/host/path:/result`.
				 * Alternatively we can `mkdir -p /result` to make it clear.
				 */
				Arrays.asList(new String[]{"sh", "-c", "touch /result/this.txt && ls /result"}), 
				new HashMap<String, String>(),
				mounts);
		
		assertEquals(run.getExitCode(), 0);
//		assertNotNull(run.getRunOutputObjectReferral());
		
		ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
		verify(minioRepository, times(1)).putRunOutputAndUpdateRunBean(eq(run), inputStreamCaptor.capture());
		assertEquals(IOUtils.toString(inputStreamCaptor.getValue(), StandardCharsets.UTF_8.name()), "this.txt\n");
		
		assertEquals(hostMountPoint.list().length, 1);
		assertEquals(hostMountPoint.list()[0], "this.txt");
	}
}
