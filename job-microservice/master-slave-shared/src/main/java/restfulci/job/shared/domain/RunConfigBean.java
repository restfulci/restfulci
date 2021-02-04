package restfulci.job.shared.domain;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RunConfigBean {
	
	@Setter
	@Getter
	public static class RunConfigExecutorBean {

		private String image;
		private RunConfigBuildBean build;
		private Map<String, String> environment = new HashMap<String, String>();
	}
	
	@Setter
	@Getter
	public static class RunConfigSidecarBean {

		/*
		 * TODO:
		 * Should sidecar support build?
		 */
		private String name;
		private String image;
		private List<String> command;
		private Map<String, String> environment = new HashMap<String, String>();
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
	
	private RunConfigExecutorBean executor;
	private List<RunConfigSidecarBean> sidecars = new ArrayList<RunConfigSidecarBean>();
	private List<String> command;
	private List<RunConfigResultBean> results = new ArrayList<RunConfigResultBean>();
	
	public File getBaseDir(Path localRepoPath) {
		return localRepoPath
				.resolve(this.getExecutor().getBuild().getContext())
				.toFile();
	}
	
	public File getDockerfile(Path localRepoPath) {
		return localRepoPath
				.resolve(this.getExecutor().getBuild().getContext())
				.resolve(this.getExecutor().getBuild().getDockerfile())
				.toFile();
	}
}
