package restfulci.shared.domain;

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
