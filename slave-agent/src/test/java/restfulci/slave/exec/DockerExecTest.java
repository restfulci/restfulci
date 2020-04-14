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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
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
import restfulci.shared.domain.RunPhase;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class DockerExecTest {
	
	@Autowired private DockerExec exec;
	
	@MockBean private RunRepository runRepository;
	@MockBean private MinioRepository minioRepository;
	
	@Test
	public void testRunCommand() throws Exception {
		
		RunBean run = new FreestyleRunBean();
		run.setPhase(RunPhase.IN_PROGRESS);
		
		String[] command = new String[] {"sh", "-c", "echo \"Hello world\""};
		exec.runCommand(run, "busybox:1.31", Arrays.asList(command), new HashMap<String, File>());
		
		ArgumentCaptor<RunBean> runCaptor = ArgumentCaptor.forClass(RunBean.class);
		verify(runRepository, times(1)).saveAndFlush(runCaptor.capture());
		assertEquals(runCaptor.getValue().getPhase(), RunPhase.COMPLETE);
		
		ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
		verify(minioRepository, times(1)).putRunOutputAndUpdateRunBean(eq(run), inputStreamCaptor.capture());
		assertEquals(IOUtils.toString(inputStreamCaptor.getValue(), StandardCharsets.UTF_8.name()), "Hello world\n");
	}
	
	/*
	 * This test can pass locally but will fail CircleCI with error:
	 * > assertEquals(hostMountPoint.list().length, 1);
	 * > org.opentest4j.AssertionFailedError: expected: <0> but was: <1>
	 * 
	 * Should be CircleCI disables docker volume/mount.
	 */
	@Disabled
//	@Test
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
		 * Both `@TempDir` and `Files.createTempDirectory` have the same problem.
		 */
		File hostMountPoint = new File(tmpFolder, "result");
		hostMountPoint.mkdir();
		
		Map<String, File> mounts = new HashMap<String, File>();
		mounts.put("/result", hostMountPoint);
		
		RunBean run = new FreestyleRunBean();
		
		String[] command = new String[] {"sh", "-c", "mkdir -p /result && touch /result/this.txt && ls /result"};
		exec.runCommand(run, "busybox:1.31", Arrays.asList(command), mounts);
		
		ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
		verify(minioRepository, times(1)).putRunOutputAndUpdateRunBean(eq(run), inputStreamCaptor.capture());
		assertEquals(IOUtils.toString(inputStreamCaptor.getValue(), StandardCharsets.UTF_8.name()), "this.txt\n");
		
		assertEquals(hostMountPoint.list().length, 1);
		assertEquals(hostMountPoint.list()[0], "this.txt");
	}
}
