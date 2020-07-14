package restfulci.pipeline.exception;

import java.io.IOException;

public class ApiPathVariableException extends IOException {

	private static final long serialVersionUID = 1L;
	
	public ApiPathVariableException(String message) {
		super(message);
	}
}
