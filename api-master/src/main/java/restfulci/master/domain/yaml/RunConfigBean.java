package restfulci.master.domain.yaml;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RunConfigBean {

	private String version;
	
	private RunConfigEnvironmentBean environment;
	private List<RunConfigRunBean> commands;
	private List<RunConfigResultBean> results;
}
