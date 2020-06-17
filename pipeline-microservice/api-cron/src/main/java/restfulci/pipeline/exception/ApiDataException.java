package restfulci.pipeline.exception;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.BAD_REQUEST)
public class ApiDataException extends IOException {

	private static final long serialVersionUID = 1L;
	
	public ApiDataException(String message) {
		super(message);
	}
}
