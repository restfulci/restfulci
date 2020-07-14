package restfulci.pipeline.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/*
 * Or should it be `CONFLICT`? 
 * Should for sure be a 4xx rather than a 5xx.
 */
@ResponseStatus(value=HttpStatus.PRECONDITION_FAILED)
public class IncompleteParameterLinkException extends BackendException {

	private static final long serialVersionUID = 1L;
	
	public IncompleteParameterLinkException(String message) {
		super(message);
	}
}
