package restfulci.slave.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/*
 * TODO:
 * Deprecate it. Should be replaced by InputStream.
 */
@Getter
@Setter
@ToString
public class DockerRunCmdResultDTO {

	private int exitCode;
	private String output;
}
