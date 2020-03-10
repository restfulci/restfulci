package restfulci.shared.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DockerRunCmdResultBean {

	private int exitCode;
	private String output;
}
