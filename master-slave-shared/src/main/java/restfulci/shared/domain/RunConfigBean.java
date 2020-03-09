package restfulci.shared.domain;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RunConfigBean {
	
	@Setter
	@Getter
	public static class RunConfigEnvironmentBean {

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
	private List<RunConfigResultBean> results;
}
