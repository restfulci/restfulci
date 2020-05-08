package restfulci.slave.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
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

import restfulci.shared.dao.MinioRepository;
import restfulci.shared.dao.RemoteGitRepository;
import restfulci.shared.dao.RunRepository;
import restfulci.shared.dao.RunResultRepository;
import restfulci.shared.domain.FreestyleJobBean;
import restfulci.shared.domain.FreestyleRunBean;
import restfulci.shared.domain.GitBranchRunBean;
import restfulci.shared.domain.GitJobBean;
import restfulci.shared.domain.GitRunBean;
import restfulci.shared.domain.InputBean;
import restfulci.shared.domain.ParameterBean;
import restfulci.shared.domain.RunBean;
import restfulci.shared.domain.RunMessageBean;
import restfulci.shared.domain.RunPhase;
import restfulci.shared.domain.RunResultBean;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class DockerRunServiceTest {
	
	@Autowired private DockerRunService service;
	
	@MockBean private RunRepository runRepository;
	@MockBean private RunResultRepository runResultRepository;
	@MockBean private MinioRepository minioRepository;
	@SpyBean private RemoteGitRepository remoteGitRepository;
	
	@Test
	public void testRunFreestyleJob() throws Exception{
		
		RunMessageBean runMessage = new RunMessageBean();
		runMessage.setJobId(123);
		runMessage.setRunId(456);
		
		FreestyleJobBean job = new FreestyleJobBean();
		job.setId(123);
		job.setName("job");
		job.setDockerImage("busybox:1.31");
		job.setCommand(new String[] {"sh", "-c", "echo \"Hello world\""});
		
		FreestyleRunBean run = new FreestyleRunBean();
		run.setId(456);
		run.setJob(job);
		run.setPhase(RunPhase.IN_PROGRESS);
		run.setTriggerAt(new Date(0L));
		run.setCompleteAt(new Date(1000L));
		
		Optional<RunBean> maybeRun = Optional.of(run);
		given(runRepository.findById(456)).willReturn(maybeRun);
		
		service.runByMessage(runMessage);
		
		ArgumentCaptor<RunBean> runCaptor = ArgumentCaptor.forClass(RunBean.class);
		verify(runRepository, times(1)).saveAndFlush(runCaptor.capture());
		assertTrue(runCaptor.getValue() instanceof FreestyleRunBean);
		assertEquals(runCaptor.getValue().getPhase(), RunPhase.COMPLETE);
//		assertNotNull(runCaptor.getValue().getRunOutputObjectReferral());
		
		ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
		verify(minioRepository, times(1)).putRunOutputAndUpdateRunBean(eq(run), inputStreamCaptor.capture());
		assertEquals(IOUtils.toString(inputStreamCaptor.getValue(), StandardCharsets.UTF_8.name()), "Hello world\n");
	}
	
	@Test
	public void testRunFreestyleJobWithInput() throws Exception{
		
		RunMessageBean runMessage = new RunMessageBean();
		runMessage.setJobId(123);
		runMessage.setRunId(456);
		
		FreestyleJobBean job = new FreestyleJobBean();
		job.setId(123);
		job.setName("job");
		job.setDockerImage("busybox:1.31");
		job.setCommand(new String[] {"sh", "-c", "echo \"Hello $WORD\""});
		
		ParameterBean parameter = new ParameterBean();
		parameter.setName("WORD");
		job.addParameter(parameter);
		
		FreestyleRunBean run = new FreestyleRunBean();
		run.setId(456);
		run.setJob(job);
		run.setPhase(RunPhase.IN_PROGRESS);
		run.setTriggerAt(new Date(0L));
		run.setCompleteAt(new Date(1000L));
		
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
		assertEquals(runCaptor.getValue().getPhase(), RunPhase.COMPLETE);
		
		ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
		verify(minioRepository, times(1)).putRunOutputAndUpdateRunBean(eq(run), inputStreamCaptor.capture());
		assertEquals(IOUtils.toString(inputStreamCaptor.getValue(), StandardCharsets.UTF_8.name()), "Hello customized input\n");
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
	
	private void testRunGitJobHelloWorld(String resourceName) throws Exception {
		
		RunMessageBean runMessage = new RunMessageBean();
		runMessage.setJobId(123);
		runMessage.setRunId(456);
		
		GitJobBean job = new GitJobBean();
		job.setId(123);
		job.setName("job");
		job.setRemoteOrigin("git@github.com:dummy/dummy.git");
		job.setConfigFilepath("restfulci.yml");
		
		GitBranchRunBean run = new GitBranchRunBean();
		run.setId(456);
		run.setJob(job);
		run.setBranchName("master");
		run.setPhase(RunPhase.IN_PROGRESS);
		run.setTriggerAt(new Date(0L));
		run.setCompleteAt(new Date(1000L));
		
		Optional<RunBean> maybeRun = Optional.of(run);
		given(runRepository.findById(456)).willReturn(maybeRun);
		
		mockGit(resourceName);
		
		service.runByMessage(runMessage);
		
		ArgumentCaptor<RunBean> runCaptor = ArgumentCaptor.forClass(RunBean.class);
		verify(runRepository, times(1)).saveAndFlush(runCaptor.capture());
		assertTrue(runCaptor.getValue() instanceof GitRunBean);
		assertEquals(runCaptor.getValue().getPhase(), RunPhase.COMPLETE);
		
		ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
		verify(minioRepository, times(1)).putRunConfigurationAndUpdateRunBean(eq(run), inputStreamCaptor.capture());
		assertEquals(
				IOUtils.toString(inputStreamCaptor.getValue(), StandardCharsets.UTF_8.name()),
				FileUtils.readFileToString(
						new File(getClass().getClassLoader().getResource("docker-run-service-test/"+resourceName+"/restfulci.yml").getFile()), 
						StandardCharsets.UTF_8));
		verify(minioRepository, times(1)).putRunOutputAndUpdateRunBean(eq(run), inputStreamCaptor.capture());
		assertEquals(IOUtils.toString(inputStreamCaptor.getValue(), StandardCharsets.UTF_8.name()), "Hello world\n");
	}
	
	@Test
	public void testRunGitJobWithParameters() throws Exception {
		
		final String resourceName = "git-with-parameters";
		
		RunMessageBean runMessage = new RunMessageBean();
		runMessage.setJobId(123);
		runMessage.setRunId(456);
		
		GitJobBean job = new GitJobBean();
		job.setId(123);
		job.setName("job");
		job.setRemoteOrigin("git@github.com:dummy/dummy.git");
		job.setConfigFilepath("restfulci.yml");
		
		ParameterBean parameter = new ParameterBean();
		parameter.setName("WORD");
		job.addParameter(parameter);
		
		GitBranchRunBean run = new GitBranchRunBean();
		run.setId(456);
		run.setJob(job);
		run.setBranchName("master");
		run.setPhase(RunPhase.IN_PROGRESS);
		run.setTriggerAt(new Date(0L));
		run.setCompleteAt(new Date(1000L));
		
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
		assertEquals(runCaptor.getValue().getPhase(), RunPhase.COMPLETE);
		
		ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
		verify(minioRepository, times(1)).putRunConfigurationAndUpdateRunBean(eq(run), inputStreamCaptor.capture());
		assertEquals(
				IOUtils.toString(inputStreamCaptor.getValue(), StandardCharsets.UTF_8.name()),
				FileUtils.readFileToString(
						new File(getClass().getClassLoader().getResource("docker-run-service-test/"+resourceName+"/restfulci.yml").getFile()), 
						StandardCharsets.UTF_8));
		verify(minioRepository, times(1)).putRunOutputAndUpdateRunBean(eq(run), inputStreamCaptor.capture());
		assertEquals(IOUtils.toString(inputStreamCaptor.getValue(), StandardCharsets.UTF_8.name()), "Hello customized input\n");
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
		
		RunMessageBean runMessage = new RunMessageBean();
		runMessage.setJobId(123);
		runMessage.setRunId(456);
		
		GitJobBean job = new GitJobBean();
		job.setId(123);
		job.setName("job");
		job.setRemoteOrigin("git@github.com:dummy/dummy.git");
		job.setConfigFilepath("restfulci.yml");
		
		GitBranchRunBean run = new GitBranchRunBean();
		run.setId(456);
		run.setJob(job);
		run.setBranchName("master");
		run.setPhase(RunPhase.IN_PROGRESS);
		run.setTriggerAt(new Date(0L));
		run.setCompleteAt(new Date(1000L));
		
		Optional<RunBean> maybeRun = Optional.of(run);
		given(runRepository.findById(456)).willReturn(maybeRun);
		
		mockGit(resourceName);
		
		service.runByMessage(runMessage);
		
		ArgumentCaptor<RunBean> runCaptor = ArgumentCaptor.forClass(RunBean.class);
		verify(runRepository, times(1)).saveAndFlush(runCaptor.capture());
		assertTrue(runCaptor.getValue() instanceof GitRunBean);
		assertEquals(runCaptor.getValue().getPhase(), RunPhase.COMPLETE);
		assertEquals(runCaptor.getValue().getRunResults().size(), 1);
		RunResultBean runResult = runCaptor.getValue().getRunResults().get(0);
		assertEquals(runResult.getType(), "plain-text");
		assertEquals(runResult.getContainerPath(), "/result");
	
		ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
		
		verify(minioRepository, times(1)).putRunConfigurationAndUpdateRunBean(eq(run), inputStreamCaptor.capture());
		assertEquals(
				IOUtils.toString(inputStreamCaptor.getValue(), StandardCharsets.UTF_8.name()),
				FileUtils.readFileToString(
						new File(getClass().getClassLoader().getResource("docker-run-service-test/"+resourceName+"/restfulci.yml").getFile()), 
						StandardCharsets.UTF_8));
		
		verify(minioRepository, times(1)).putRunOutputAndUpdateRunBean(eq(run), inputStreamCaptor.capture());
		assertEquals(IOUtils.toString(inputStreamCaptor.getValue(), StandardCharsets.UTF_8.name()), "this.txt\n");
		
		verify(minioRepository, times(1)).putRunResultAndUpdateRunResultBean(
				eq(runResult), inputStreamCaptor.capture());
		File zipFile = new File(tempFolder, "zip.zip");
		File resultFolder = new File(tempFolder, "result");
		resultFolder.mkdir();
		FileUtils.copyInputStreamToFile(inputStreamCaptor.getValue(), zipFile);
		ZipUtil.unpack(zipFile, resultFolder);
		assertEquals(resultFolder.list().length, 1);
		assertEquals(resultFolder.list()[0], "this.txt");
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
