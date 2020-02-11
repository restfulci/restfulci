package restfulci.master.domain.yaml;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RunConfigBuildBean {

	private String context;
	private String dockerfile;
}
