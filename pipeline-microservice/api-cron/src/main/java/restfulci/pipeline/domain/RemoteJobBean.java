package restfulci.pipeline.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AccessLevel;
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
		
		@Getter(AccessLevel.NONE)
		private String defaultValue;
		
		public Boolean isOptional() {
			if (defaultValue == null) {
				return false;
			}
			else {
				return true;
			}
		}
	}

	private Integer id;
	
	/*
	 * TODO:
	 * Map to enum?
	 */
	private String name;
	
	private String type;
	
	private List<Parameter> parameters;
	
	public Parameter getParameter(String name) {
		for (Parameter parameter : parameters) {
			if (parameter.getName().equals(name)) {
				return parameter;
			}
		}
		return null;
	}
}
