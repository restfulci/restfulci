package restfulci.pipeline.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown=true)
public class RemoteJobBean {
	
	@Getter
	@Setter
	public static class Parameter {
		
		private Integer id;
		private String name;
	}

	private Integer id;
	
	/*
	 * TODO:
	 * Map to enum?
	 */
	private String name;
	
	private String type;
	
	private List<Parameter> parameters;
}
