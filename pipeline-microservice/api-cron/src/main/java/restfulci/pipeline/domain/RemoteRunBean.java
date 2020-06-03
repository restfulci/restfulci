package restfulci.pipeline.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown=true)
public class RemoteRunBean {

	private Integer id;
	
	/*
	 * TODO:
	 * Map to enum?
	 */
	private String status;
	
	private Integer exitCode;
}
