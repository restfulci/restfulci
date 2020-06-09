package restfulci.pipeline.exception;

import java.io.IOException;

public class JobMicroserviceCallException extends IOException {

	private static final long serialVersionUID = 1L;
	
	public JobMicroserviceCallException(String message) {
		super(message);
	}
}
