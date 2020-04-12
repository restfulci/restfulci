package restfulci.slave.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import restfulci.shared.dao.RemoteGitRepository;
import restfulci.shared.dao.RunRepository;
import restfulci.shared.domain.FreestyleJobBean;
import restfulci.shared.domain.FreestyleRunBean;
import restfulci.shared.domain.GitRunBean;
import restfulci.shared.domain.RunBean;
import restfulci.shared.domain.RunConfigBean;
import restfulci.shared.domain.RunMessageBean;
import restfulci.slave.exec.DockerExec;

@Service
public class DockerRunServiceImpl implements DockerRunService {
	
	@Autowired private DockerExec dockerExec;
	
	@Autowired private RunRepository runRepository;
	@Autowired private RemoteGitRepository remoteGitRepository;

	@Override
	public void runByMessage(RunMessageBean runMessage) throws InterruptedException, IOException {
		
		RunBean run = runRepository.findById(runMessage.getRunId()).get();
		
		if (run instanceof FreestyleRunBean) {
			FreestyleRunBean freestyleRun = (FreestyleRunBean)run;
			runFreestyleJob(freestyleRun);
		}
		else if (run instanceof GitRunBean) {
			GitRunBean gitRun = (GitRunBean)run;
			runGitJob(gitRun);
		}
		else {
			throw new IOException("Input run with wrong type");
		}
	}
	
	private void runFreestyleJob(FreestyleRunBean run) throws InterruptedException {
		FreestyleJobBean job = run.getJob();
		dockerExec.pullImage(job.getDockerImage());
		dockerExec.runCommand(run, job.getDockerImage(), Arrays.asList(job.getCommand()));
	}
	
	private void runGitJob(GitRunBean run) throws InterruptedException, IOException {
		
		/*
		 * Steps:
		 * (1) Create temporary local folder.
		 * (2) Clone (by branch name or commit SHA) single commit into the local folder.
		 * (3) Find the config file in local folder and generate `RunConfigBean`.
		 * (4) Build image, run job based on config file and the contents in git clone.
		 * (5) Clean up docker container (TODO).
		 * (6) Clean up the temporary local folder (TODO).
		 */
		Path localRepoPath = Files.createTempDirectory("local-repo");
		remoteGitRepository.copyToLocal(run, localRepoPath);
		RunConfigBean runConfig = remoteGitRepository.getConfigFromFilepath(run, localRepoPath);
	
		String imageId = dockerExec.buildImageAndGetId(localRepoPath, runConfig);
		dockerExec.runCommand(run, imageId, runConfig.getCommand());
	}
}
