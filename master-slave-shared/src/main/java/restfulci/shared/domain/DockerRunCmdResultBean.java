package restfulci.shared.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DockerRunCmdResultBean {

	private int exitCode;
	private String output;
}
