package restfulci.pipeline.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.NOT_FOUND)
public class IdNonExistenceException extends ApiPathVariableException {

	private static final long serialVersionUID = 1L;
	
	public IdNonExistenceException(String message) {
		super(message);
	}
}
