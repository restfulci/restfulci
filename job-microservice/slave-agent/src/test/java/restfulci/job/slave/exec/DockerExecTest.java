package restfulci.job.slave.exec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import com.github.dockerjava.api.exception.BadRequestException;
import com.github.dockerjava.api.model.Network;

import restfulci.job.shared.dao.MinioRepository;
import restfulci.job.shared.dao.RunRepository;
import restfulci.job.shared.domain.FreestyleRunBean;
import restfulci.job.shared.domain.RunBean;
import restfulci.job.shared.domain.RunConfigBean;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class DockerExecTest {
	
	@Autowired private DockerExec exec;
	
	@MockBean private RunRepository runRepository;
	@MockBean private MinioRepository minioRepository;
	
	private final String containerName = "restfulci-unit-test-container";
	
	@Test
	public void testCreateNetwork() throws Exception {
		Network network = exec.createNetworkIfNotExist("restfulci-unit-test");
		assertEquals(network.getName(), "restfulci-unit-test");
		
		Network networkAgain = exec.createNetworkIfNotExist("restfulci-unit-test");
		assertEquals(networkAgain.getName(), "restfulci-unit-test");
	}
	
	@Test
	public void testPullImage() throws Exception {
		exec.pullImage("busybox");
		exec.pullImage("busybox:1.33");
		exec.pullImage("busybox:latest");
	}
	
	@Test
	public void testCreateAndKillSidecarWithNullCommand() throws Exception {
		
		exec.pullImage("postgres:13.1");
		String containerId = exec.createSidecar(
				"postgres:13.1", 
				containerName, 
				"bridge",
				null,
				new HashMap<String, String>());
		exec.killSidecar(containerId);
	}
	
	@Test
	public void testCreateAndKillSidecarWithNonnullCommand() throws Exception {
		
		exec.pullImage("busybox:1.33");
		String containerId = exec.createSidecar(
				"busybox:1.33", 
				containerName, 
				"bridge",
				Arrays.asList(new String[]{"sleep", "infinity"}),
				new HashMap<String, String>());
		exec.killSidecar(containerId);
	}
	
	@Test
	public void testCreateAndKillSidecarWhichMayHaveStoppedRunning() throws Exception {
		
		exec.pullImage("busybox:1.33");
		String containerId = exec.createSidecar(
				"busybox:1.33", 
				containerName, 
				"bridge",
				null,
				new HashMap<String, String>());
		exec.killSidecar(containerId);
	}
	
	@Test
	public void testRunCommand() throws Exception {
		
		RunBean run = new FreestyleRunBean();
		
		exec.pullImage("busybox:1.33");
		exec.runCommandAndUpdateRunBean(
				run, 
				"busybox:1.33",
				containerName,
				"bridge",
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
		verify(minioRepository, times(1)).putRunOutputAndReturnObjectName(
				inputStreamCaptor.capture(), eq(run.getDefaultRunOutputObjectReferral()));
		assertEquals(
				IOUtils.toString(inputStreamCaptor.getValue(), StandardCharsets.UTF_8.name()), 
				"Hello world\n");
	}
	
	@Test
	public void testRunCommandWithInput() throws Exception {
		
		RunBean run = new FreestyleRunBean();
		
		Map<String, String> envVars = new HashMap<String, String>();
		envVars.put("WORD", "customized input");
		
		exec.pullImage("busybox:1.33");
		exec.runCommandAndUpdateRunBean(
				run, 
				"busybox:1.33", 
				containerName,
				"bridge",
				Arrays.asList(new String[]{"sh", "-c", "echo \"Hello $WORD\""}), 
				envVars,
				new HashMap<RunConfigBean.RunConfigResultBean, File>());
		
		assertEquals(run.getExitCode(), 0);
		/*
		 * Cannot assert this, as the set logic is in `minioRepository`
		 * which is mocked here.
		 */
//		assertNotNull(run.getRunOutputObjectReferral());
		
		ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
		verify(minioRepository, times(1)).putRunOutputAndReturnObjectName(
				inputStreamCaptor.capture(), eq(run.getDefaultRunOutputObjectReferral()));
		assertEquals(
				IOUtils.toString(inputStreamCaptor.getValue(), StandardCharsets.UTF_8.name()), 
				"Hello customized input\n");
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
		
		exec.pullImage("busybox:1.33");
		exec.runCommandAndUpdateRunBean(
				run, 
				"busybox:1.33", 
				containerName,
				"bridge",
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
		verify(minioRepository, times(1)).putRunOutputAndReturnObjectName(
				inputStreamCaptor.capture(), eq(run.getDefaultRunOutputObjectReferral()));
		assertEquals(
				IOUtils.toString(inputStreamCaptor.getValue(), StandardCharsets.UTF_8.name()), 
				"this.txt\n");
		
		assertEquals(hostMountPoint.list().length, 1);
		assertEquals(hostMountPoint.list()[0], "this.txt");
	}
	
	@Test
	public void testRunFailedCommand() throws Exception {
		
		RunBean run = new FreestyleRunBean();
		
		exec.pullImage("busybox:1.33");
		exec.runCommandAndUpdateRunBean(
				run, 
				"busybox:1.33",
				containerName,
				"bridge",
				Arrays.asList(new String[]{"sh", "-c", "exit 1"}), 
				new HashMap<String, String>(),
				new HashMap<RunConfigBean.RunConfigResultBean, File>());
		
		assertEquals(run.getExitCode(), 1);
	}
	
	@Test
	public void testRunInvalidCommandInsideShell() throws Exception {
		
		RunBean run = new FreestyleRunBean();
		
		exec.pullImage("busybox:1.33");
		exec.runCommandAndUpdateRunBean(
				run, 
				"busybox:1.33",
				containerName,
				"bridge",
				Arrays.asList(new String[]{"sh", "-c", "invalid"}), 
				new HashMap<String, String>(),
				new HashMap<RunConfigBean.RunConfigResultBean, File>());
		
		assertEquals(run.getExitCode(), 127);
		
		ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
		verify(minioRepository, times(1)).putRunOutputAndReturnObjectName(
				inputStreamCaptor.capture(), eq(run.getDefaultRunOutputObjectReferral()));
		assertEquals(
				IOUtils.toString(inputStreamCaptor.getValue(), StandardCharsets.UTF_8.name()), 
				"sh: invalid: not found\n");
	}
	
	@Test
	public void testRunInvalidCommandOutsideShell() throws Exception {
		
		RunBean run = new FreestyleRunBean();
		exec.pullImage("busybox:1.33");
		
		assertThrows(BadRequestException.class, () -> {
			exec.runCommandAndUpdateRunBean(
					run, 
					"busybox:1.33",
					containerName,
					"bridge",
					Arrays.asList(new String[]{"invalid"}), 
					new HashMap<String, String>(),
					new HashMap<RunConfigBean.RunConfigResultBean, File>());
		});
	}
}
