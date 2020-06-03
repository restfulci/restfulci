package restfulci.job.shared.domain;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RunConfigBean {
	
	@Setter
	@Getter
	public static class RunConfigEnvironmentBean {

		private String image;
		private RunConfigBuildBean build;
	}
	
	@Setter
	@Getter
	public static class RunConfigBuildBean {

		private String context;
		private String dockerfile;
	}
	
	@Setter
	@Getter
	public static class RunConfigResultBean {

		private String type;
		
		/*
		 * TODO:
		 * It is a hard limit that this `path` need to be an absolute path inside of
		 * the container. So when there's a `WORKDIR` setup, you need to include that
		 * `WORKDIR` into the path. In docker community there are discussion on add
		 * container relative path into volume mount, but it has no been addressed yet.
		 * https://github.com/moby/moby/issues/4830
		 * https://github.com/docker/cli/issues/1203
		 */
		private String path;
	}

	private String version;
	
	private RunConfigEnvironmentBean environment;
	private List<String> command;
	private List<RunConfigResultBean> results = new ArrayList<RunConfigResultBean>();
	
	public File getBaseDir(Path localRepoPath) {
		return localRepoPath
				.resolve(this.getEnvironment().getBuild().getContext())
				.toFile();
	}
	
	public File getDockerfile(Path localRepoPath) {
		return localRepoPath
				.resolve(this.getEnvironment().getBuild().getContext())
				.resolve(this.getEnvironment().getBuild().getDockerfile())
				.toFile();
	}
}