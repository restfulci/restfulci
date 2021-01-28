package restfulci.job.slave.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.zeroturnaround.zip.ZipUtil;

import restfulci.job.shared.dao.MinioRepository;
import restfulci.job.shared.dao.RemoteGitRepository;
import restfulci.job.shared.dao.RunRepository;
import restfulci.job.shared.dao.RunResultRepository;
import restfulci.job.shared.domain.FreestyleJobBean;
import restfulci.job.shared.domain.FreestyleRunBean;
import restfulci.job.shared.domain.GitBranchRunBean;
import restfulci.job.shared.domain.GitJobBean;
import restfulci.job.shared.domain.GitRunBean;
import restfulci.job.shared.domain.InputBean;
import restfulci.job.shared.domain.ParameterBean;
import restfulci.job.shared.domain.RunBean;
import restfulci.job.shared.domain.RunMessageBean;
import restfulci.job.shared.domain.RunStatus;
import restfulci.job.shared.domain.RunResultBean;
import restfulci.job.slave.service.DockerRunService;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class DockerRunServiceTest {
	
	@Autowired private DockerRunService service;
	
	@MockBean private RunRepository runRepository;
	@MockBean private RunResultRepository runResultRepository;
	@MockBean private MinioRepository minioRepository;
	@SpyBean private RemoteGitRepository remoteGitRepository;
	
	@Test
	public void testRunSuccessfulFreestyleJob() throws Exception{
		
		RunMessageBean runMessage = new RunMessageBean();
		runMessage.setJobId(123);
		runMessage.setRunId(456);
		
		FreestyleJobBean job = new FreestyleJobBean();
		job.setId(123);
		job.setDockerImage("busybox:1.33");
		job.setCommand(new String[] {"sh", "-c", "echo \"Hello world\""});
		
		FreestyleRunBean run = new FreestyleRunBean();
		run.setId(456);
		run.setJob(job);
		run.setStatus(RunStatus.IN_PROGRESS);
		run.setTriggerAt(new Date(0L));
		
		Optional<RunBean> maybeRun = Optional.of(run);
		given(runRepository.findById(456)).willReturn(maybeRun);
		
		service.runByMessage(runMessage);
		
		ArgumentCaptor<RunBean> runCaptor = ArgumentCaptor.forClass(RunBean.class);
		verify(runRepository, times(1)).saveAndFlush(runCaptor.capture());
		assertTrue(runCaptor.getValue() instanceof FreestyleRunBean);
		assertNotNull(runCaptor.getValue().getCompleteAt());
		assertEquals(runCaptor.getValue().getStatus(), RunStatus.SUCCEED);
//		assertNotNull(runCaptor.getValue().getRunOutputObjectReferral());
		
		ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
		verify(minioRepository, times(1)).putRunOutputAndReturnObjectName(
				inputStreamCaptor.capture(), eq(run.getDefaultRunOutputObjectReferral()));
		assertEquals(
				IOUtils.toString(inputStreamCaptor.getValue(), StandardCharsets.UTF_8.name()), 
				"Hello world\n");
	}
	
	@Test
	public void testRunFailedFreestyleJob() throws Exception{
		
		RunMessageBean runMessage = getMockRunMessage();
		
		FreestyleJobBean job = new FreestyleJobBean();
		job.setId(123);
		job.setDockerImage("busybox:1.33");
		job.setCommand(new String[] {"sh", "-c", "echx \"Hello world\""});
		
		FreestyleRunBean run = new FreestyleRunBean();
		run.setId(456);
		run.setJob(job);
		run.setStatus(RunStatus.IN_PROGRESS);
		run.setTriggerAt(new Date(0L));
		
		Optional<RunBean> maybeRun = Optional.of(run);
		given(runRepository.findById(456)).willReturn(maybeRun);
		
		service.runByMessage(runMessage);
		
		ArgumentCaptor<RunBean> runCaptor = ArgumentCaptor.forClass(RunBean.class);
		verify(runRepository, times(1)).saveAndFlush(runCaptor.capture());
		assertTrue(runCaptor.getValue() instanceof FreestyleRunBean);
		assertNotNull(runCaptor.getValue().getCompleteAt());
		assertEquals(runCaptor.getValue().getStatus(), RunStatus.FAIL);
		
		ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
		verify(minioRepository, times(1)).putRunOutputAndReturnObjectName(
				inputStreamCaptor.capture(), eq(run.getDefaultRunOutputObjectReferral()));
		assertEquals(
				IOUtils.toString(inputStreamCaptor.getValue(), StandardCharsets.UTF_8.name()), 
				"sh: echx: not found\n");
	}
	
	@Test
	public void testRunFreestyleJobWithInput() throws Exception{
		
		RunMessageBean runMessage = new RunMessageBean();
		runMessage.setJobId(123);
		runMessage.setRunId(456);
		
		FreestyleJobBean job = new FreestyleJobBean();
		job.setId(123);
		job.setDockerImage("busybox:1.33");
		job.setCommand(new String[] {"sh", "-c", "echo \"Hello $WORD\""});
		
		ParameterBean parameter = new ParameterBean();
		parameter.setName("WORD");
		job.addParameter(parameter);
		
		FreestyleRunBean run = new FreestyleRunBean();
		run.setId(456);
		run.setJob(job);
		run.setStatus(RunStatus.IN_PROGRESS);
		run.setTriggerAt(new Date(0L));
		
		InputBean input = new InputBean();
		input.setName("WORD");
		input.setValue("customized input");
		run.addInput(input);
		
		Optional<RunBean> maybeRun = Optional.of(run);
		given(runRepository.findById(456)).willReturn(maybeRun);
		
		service.runByMessage(runMessage);
		
		ArgumentCaptor<RunBean> runCaptor = ArgumentCaptor.forClass(RunBean.class);
		verify(runRepository, times(1)).saveAndFlush(runCaptor.capture());
		assertTrue(runCaptor.getValue() instanceof FreestyleRunBean);
		assertNotNull(runCaptor.getValue().getCompleteAt());
		assertEquals(runCaptor.getValue().getStatus(), RunStatus.SUCCEED);
		
		ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
		verify(minioRepository, times(1)).putRunOutputAndReturnObjectName(
				inputStreamCaptor.capture(), eq(run.getDefaultRunOutputObjectReferral()));
		assertEquals(
				IOUtils.toString(inputStreamCaptor.getValue(), StandardCharsets.UTF_8.name()), 
				"Hello customized input\n");
	}
	
	@Test
	public void testRunFreestyleJobWithInvalidImage() throws Exception{
		
		RunMessageBean runMessage = new RunMessageBean();
		runMessage.setJobId(123);
		runMessage.setRunId(456);
		
		FreestyleJobBean job = new FreestyleJobBean();
		job.setId(123);
		job.setDockerImage("notexistbox:1.2345");
		job.setCommand(new String[] {"sh", "-c", "echo \"Hello world\""});
		
		FreestyleRunBean run = new FreestyleRunBean();
		run.setId(456);
		run.setJob(job);
		run.setStatus(RunStatus.IN_PROGRESS);
		run.setTriggerAt(new Date(0L));
		
		Optional<RunBean> maybeRun = Optional.of(run);
		given(runRepository.findById(456)).willReturn(maybeRun);
		
		service.runByMessage(runMessage);
		
		ArgumentCaptor<RunBean> runCaptor = ArgumentCaptor.forClass(RunBean.class);
		verify(runRepository, times(1)).saveAndFlush(runCaptor.capture());
		assertTrue(runCaptor.getValue() instanceof FreestyleRunBean);
		assertNotNull(runCaptor.getValue().getCompleteAt());
		assertEquals(runCaptor.getValue().getStatus(), RunStatus.FAIL);
		assertTrue(runCaptor.getValue().getErrorMessage().contains("Pulling image error"));
		assertTrue(runCaptor.getValue().getErrorMessage().contains("notexistbox"));
		assertNull(runCaptor.getValue().getExitCode());
		
		verify(minioRepository, never()).putRunOutputAndReturnObjectName(
				any(InputStream.class), any(String.class));
	}
	
	@Test
	public void testRunFreestyleJobBadRequest() throws Exception{
		
		RunMessageBean runMessage = new RunMessageBean();
		runMessage.setJobId(123);
		runMessage.setRunId(456);
		
		FreestyleJobBean job = new FreestyleJobBean();
		job.setId(123);
		job.setDockerImage("busybox:1.33");
		job.setCommand(new String[] {"invalidcommand"});
		
		FreestyleRunBean run = new FreestyleRunBean();
		run.setId(456);
		run.setJob(job);
		run.setStatus(RunStatus.IN_PROGRESS);
		run.setTriggerAt(new Date(0L));
		
		Optional<RunBean> maybeRun = Optional.of(run);
		given(runRepository.findById(456)).willReturn(maybeRun);
		
		service.runByMessage(runMessage);
		
		ArgumentCaptor<RunBean> runCaptor = ArgumentCaptor.forClass(RunBean.class);
		verify(runRepository, times(1)).saveAndFlush(runCaptor.capture());
		assertTrue(runCaptor.getValue() instanceof FreestyleRunBean);
		assertNotNull(runCaptor.getValue().getCompleteAt());
		assertEquals(runCaptor.getValue().getStatus(), RunStatus.FAIL);
		assertTrue(runCaptor.getValue().getErrorMessage().contains("Invalid command"));
		assertTrue(runCaptor.getValue().getErrorMessage().contains("invalidcommand"));
		assertNull(runCaptor.getValue().getExitCode());
		
		verify(minioRepository, never()).putRunOutputAndReturnObjectName(
				any(InputStream.class), any(String.class));
	}
	
	@Test
	public void testRunGitJobDefaultFromImage() throws Exception {
		testRunGitJobHelloWorld("git-default-from-image");
	}
	
	@Test
	public void testRunGitJobDefaultFromBuild() throws Exception {
		testRunGitJobHelloWorld("git-default-from-build");
	}

	@Test
	public void testRunGitJobCustomizedBasedir() throws Exception {
		testRunGitJobHelloWorld("git-customized-basedir");
	}
	
	@Test
	public void testRunGitJobCustomizedDockerfile() throws Exception {
		testRunGitJobHelloWorld("git-customized-dockerfile");
	}
	
	@Test
	public void testRunGitJobShellBaked() throws Exception {
		testRunGitJobHelloWorld("git-shell-baked");
	}
	
	@Test
	public void testRunGitJobWithEnvironmentVaribles() throws Exception {
		testRunGitJobHelloWorld("git-with-envvar");
	}
	
	private void testRunGitJobHelloWorld(String resourceName) throws Exception {
		
		RunMessageBean runMessage = getMockRunMessage();
		GitRunBean run = getMockGitRun();
		
		Optional<RunBean> maybeRun = Optional.of(run);
		given(runRepository.findById(456)).willReturn(maybeRun);
		
		mockGit(resourceName);
		
		service.runByMessage(runMessage);
		
		ArgumentCaptor<RunBean> runCaptor = ArgumentCaptor.forClass(RunBean.class);
		verify(runRepository, times(1)).saveAndFlush(runCaptor.capture());
		assertTrue(runCaptor.getValue() instanceof GitRunBean);
		assertNotNull(runCaptor.getValue().getCompleteAt());
		assertEquals(runCaptor.getValue().getStatus(), RunStatus.SUCCEED);
		
		ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
		verify(minioRepository, times(1)).putRunConfigurationAndReturnObjectName(
				inputStreamCaptor.capture(), eq(run.getDefaultRunConfigurationObjectReferral()));
		assertEquals(
				IOUtils.toString(inputStreamCaptor.getValue(), StandardCharsets.UTF_8.name()),
				FileUtils.readFileToString(
						new File(getClass().getClassLoader().getResource("docker-run-service-test/"+resourceName+"/restfulci.yml").getFile()), 
						StandardCharsets.UTF_8));
		verify(minioRepository, times(1)).putRunOutputAndReturnObjectName(
				inputStreamCaptor.capture(), eq(run.getDefaultRunOutputObjectReferral()));
		assertEquals(
				IOUtils.toString(inputStreamCaptor.getValue(), StandardCharsets.UTF_8.name()), 
				"Hello world\n");
	}
	
	@Test
	public void testRunGitJobWithParameters() throws Exception {
		
		final String resourceName = "git-with-parameters";
		
		RunMessageBean runMessage = getMockRunMessage();
		GitRunBean run = getMockGitRun();
		
		InputBean input = new InputBean();
		input.setName("WORD");
		input.setValue("customized input");
		run.addInput(input);
		
		Optional<RunBean> maybeRun = Optional.of(run);
		given(runRepository.findById(456)).willReturn(maybeRun);
		
		mockGit(resourceName);
		
		service.runByMessage(runMessage);
		
		ArgumentCaptor<RunBean> runCaptor = ArgumentCaptor.forClass(RunBean.class);
		verify(runRepository, times(1)).saveAndFlush(runCaptor.capture());
		assertTrue(runCaptor.getValue() instanceof GitRunBean);
		assertNotNull(runCaptor.getValue().getCompleteAt());
		assertEquals(runCaptor.getValue().getStatus(), RunStatus.SUCCEED);
		
		ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
		verify(minioRepository, times(1)).putRunConfigurationAndReturnObjectName(
				inputStreamCaptor.capture(), eq(run.getDefaultRunConfigurationObjectReferral()));
		assertEquals(
				IOUtils.toString(inputStreamCaptor.getValue(), StandardCharsets.UTF_8.name()),
				FileUtils.readFileToString(
						new File(getClass().getClassLoader().getResource("docker-run-service-test/"+resourceName+"/restfulci.yml").getFile()), 
						StandardCharsets.UTF_8));
		verify(minioRepository, times(1)).putRunOutputAndReturnObjectName(
				inputStreamCaptor.capture(), eq(run.getDefaultRunOutputObjectReferral()));
		assertEquals(
				IOUtils.toString(inputStreamCaptor.getValue(), StandardCharsets.UTF_8.name()), 
				"Hello customized input\n");
	}
	
	@Test
	public void testRunGitJobWithCommandedSidecars() throws Exception {
		
		final String resourceName = "git-with-commanded-sidecars";
		/*
		 * TODO:
		 * Change alpine to busybox, dockerjava errors out with a thread error.
		 * We'll probably want to understand more on why that happens, and fix
		 * it. 
		 * 
		 * Looks like it is for case the container errors out (`invalid_cmd`)
		 * instead of failed (`exit 1`).
		 */
		
		RunMessageBean runMessage = getMockRunMessage();
		GitRunBean run = getMockGitRun();
		
		Optional<RunBean> maybeRun = Optional.of(run);
		given(runRepository.findById(456)).willReturn(maybeRun);
		
		mockGit(resourceName);
		
		service.runByMessage(runMessage);
		
		ArgumentCaptor<RunBean> runCaptor = ArgumentCaptor.forClass(RunBean.class);
		verify(runRepository, times(1)).saveAndFlush(runCaptor.capture());
		assertTrue(runCaptor.getValue() instanceof GitRunBean);
		assertNotNull(runCaptor.getValue().getCompleteAt());
		assertEquals(runCaptor.getValue().getStatus(), RunStatus.SUCCEED);
		
		ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
		verify(minioRepository, times(1)).putRunConfigurationAndReturnObjectName(
				inputStreamCaptor.capture(), eq(run.getDefaultRunConfigurationObjectReferral()));
		assertEquals(
				IOUtils.toString(inputStreamCaptor.getValue(), StandardCharsets.UTF_8.name()),
				FileUtils.readFileToString(
						new File(getClass().getClassLoader().getResource("docker-run-service-test/"+resourceName+"/restfulci.yml").getFile()), 
						StandardCharsets.UTF_8));
		verify(minioRepository, times(1)).putRunOutputAndReturnObjectName(
				inputStreamCaptor.capture(), eq(run.getDefaultRunOutputObjectReferral()));
		String consoleOutput = IOUtils.toString(
				inputStreamCaptor.getValue(), StandardCharsets.UTF_8.name());
		assertTrue(consoleOutput.contains("PING lazybox"));
		assertTrue(consoleOutput.contains("lazybox ping statistics"));
		assertTrue(consoleOutput.contains("packets transmitted"));
	}
	
	/*
	 * This test can pass locally but will fail CircleCI with error:
	 * > org.zeroturnaround.zip.ZipException: Given directory 
	 * > '/tmp/result17110808655528955129' doesn't contain any files!
	 * 
	 * Should be CircleCI disables docker volume/mount.
	 */
	@Test
	@DisabledIfEnvironmentVariable(named="CI", matches="CircleCI")
	public void testRunGitJobWithResults(@TempDir File tempFolder) throws Exception {
		
		final String resourceName = "git-with-results";
		
		RunMessageBean runMessage = getMockRunMessage();
		GitRunBean run = getMockGitRun();
		
		Optional<RunBean> maybeRun = Optional.of(run);
		given(runRepository.findById(456)).willReturn(maybeRun);
		
		mockGit(resourceName);
		
		service.runByMessage(runMessage);
		
		ArgumentCaptor<RunBean> runCaptor = ArgumentCaptor.forClass(RunBean.class);
		verify(runRepository, times(1)).saveAndFlush(runCaptor.capture());
		assertTrue(runCaptor.getValue() instanceof GitRunBean);
		assertNotNull(runCaptor.getValue().getCompleteAt());
		assertEquals(runCaptor.getValue().getStatus(), RunStatus.SUCCEED);
		assertEquals(runCaptor.getValue().getRunResults().size(), 1);
		RunResultBean runResult = new ArrayList<>(runCaptor.getValue().getRunResults()).get(0);
		assertEquals(runResult.getType(), "plain-text");
		assertEquals(runResult.getContainerPath(), "/result");
	
		ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
		
		verify(minioRepository, times(1)).putRunConfigurationAndReturnObjectName(
				inputStreamCaptor.capture(), eq(run.getDefaultRunConfigurationObjectReferral()));
		assertEquals(
				IOUtils.toString(inputStreamCaptor.getValue(), StandardCharsets.UTF_8.name()),
				FileUtils.readFileToString(
						new File(getClass().getClassLoader().getResource("docker-run-service-test/"+resourceName+"/restfulci.yml").getFile()), 
						StandardCharsets.UTF_8));
		
		verify(minioRepository, times(1)).putRunOutputAndReturnObjectName(
				inputStreamCaptor.capture(), eq(run.getDefaultRunOutputObjectReferral()));
		assertEquals(
				IOUtils.toString(inputStreamCaptor.getValue(), StandardCharsets.UTF_8.name()), 
				"this.txt\n");
		
		verify(minioRepository, times(1)).putRunResultAndReturnObjectName(
				inputStreamCaptor.capture(), eq(runResult.getDefaultObjectReferral()));
		File zipFile = new File(tempFolder, "zip.zip");
		File resultFolder = new File(tempFolder, "result");
		resultFolder.mkdir();
		FileUtils.copyInputStreamToFile(inputStreamCaptor.getValue(), zipFile);
		ZipUtil.unpack(zipFile, resultFolder);
		assertEquals(resultFolder.list().length, 1);
		assertEquals(resultFolder.list()[0], "this.txt");
	}
	
	@Test
	public void testRunGitJobFailedExecutorWillCleanupContainers() throws Exception {
		
		final String resourceName = "git-failed";
		
		RunMessageBean runMessage = getMockRunMessage();
		GitRunBean run = getMockGitRun();
		
		Optional<RunBean> maybeRun = Optional.of(run);
		given(runRepository.findById(456)).willReturn(maybeRun);
		
		mockGit(resourceName);
		
		service.runByMessage(runMessage);
		
		ArgumentCaptor<RunBean> runCaptor = ArgumentCaptor.forClass(RunBean.class);
		verify(runRepository, times(1)).saveAndFlush(runCaptor.capture());
		assertTrue(runCaptor.getValue() instanceof GitRunBean);
		assertNotNull(runCaptor.getValue().getCompleteAt());
		assertEquals(runCaptor.getValue().getStatus(), RunStatus.FAIL);
	}
	
	@Test
	public void testRunGitJobInvalidMainImage() throws Exception {
		String errorMessage = testRunGitJobAndReturnErrorMessage("git-invalid-main-image");
		
		assertTrue(errorMessage.contains("Pulling main image error"));
		assertTrue(errorMessage.contains("notexistbox"));
		
	}
	
	@Test
	public void testRunGitJobInvalidSidecarImage() throws Exception {
		String errorMessage = testRunGitJobAndReturnErrorMessage("git-invalid-sidecar-image");
		
		assertTrue(errorMessage.contains("Pulling sidecar image error"));
		assertTrue(errorMessage.contains("notexistbox"));
	}
	
	@Test
	public void testRunGitJobInvalidCommand() throws Exception {
		String errorMessage = testRunGitJobAndReturnErrorMessage("git-invalid-command");
		
		assertTrue(errorMessage.contains("Invalid command"));
		assertTrue(errorMessage.contains("invalidcommand"));
	}
	
	private String testRunGitJobAndReturnErrorMessage(String resourceName) throws Exception {

		RunMessageBean runMessage = getMockRunMessage();
		GitRunBean run = getMockGitRun();
		
		Optional<RunBean> maybeRun = Optional.of(run);
		given(runRepository.findById(456)).willReturn(maybeRun);
		
		mockGit(resourceName);
		
		service.runByMessage(runMessage);
		
		ArgumentCaptor<RunBean> runCaptor = ArgumentCaptor.forClass(RunBean.class);
		verify(runRepository, times(1)).saveAndFlush(runCaptor.capture());
		assertTrue(runCaptor.getValue() instanceof GitRunBean);
		assertNotNull(runCaptor.getValue().getCompleteAt());
		assertEquals(runCaptor.getValue().getStatus(), RunStatus.FAIL);
		assertNull(runCaptor.getValue().getExitCode());
		
		verify(minioRepository, never()).putRunOutputAndReturnObjectName(
				any(InputStream.class), any(String.class));
		
		return runCaptor.getValue().getErrorMessage();
	}
	
	private RunMessageBean getMockRunMessage() {
		
		RunMessageBean runMessage = new RunMessageBean();
		runMessage.setJobId(123);
		runMessage.setRunId(456);
		return runMessage;
	}
	
	private GitRunBean getMockGitRun() {
		
		GitJobBean job = new GitJobBean();
		job.setId(123);
		job.setName("job");
		job.setRemoteOrigin("git@github.com:dummy/dummy.git");
		job.setConfigFilepath("restfulci.yml");
		
		GitBranchRunBean run = new GitBranchRunBean();
		run.setId(456);
		run.setJob(job);
		run.setBranchName("master");
		run.setStatus(RunStatus.IN_PROGRESS);
		run.setTriggerAt(new Date(0L));
		
		return run;
	}
	
	private void mockGit(String resourceName) throws IOException, InterruptedException {
		doAnswer(new Answer<Void>() {
			public Void answer(InvocationOnMock invocation) {
				Path localRepoPath = (Path) invocation.getArguments()[1];
				try {
					FileUtils.copyDirectory(
							new File(getClass().getClassLoader().getResource("docker-run-service-test/"+resourceName).getFile()),
							localRepoPath.toFile());
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}
		 }).when(remoteGitRepository).copyToLocal(any(GitRunBean.class), any(Path.class));
	}
}
