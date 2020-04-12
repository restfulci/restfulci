package restfulci.slave.exec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
		exec.runCommand(run, "busybox:1.31", Arrays.asList(command));
		
		ArgumentCaptor<RunBean> runCaptor = ArgumentCaptor.forClass(RunBean.class);
		verify(runRepository, times(1)).saveAndFlush(runCaptor.capture());
		assertEquals(runCaptor.getValue().getPhase(), RunPhase.COMPLETE);
		
		ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
		verify(minioRepository, times(1)).putRunOutputAndUpdateRunBean(eq(run), inputStreamCaptor.capture());
		assertEquals(IOUtils.toString(inputStreamCaptor.getValue(), StandardCharsets.UTF_8.name()), "Hello world\n");
	}
}
