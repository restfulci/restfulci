package restfulci.job.shared.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.BAD_REQUEST)
public class RunInputException extends BeanException {

	private static final long serialVersionUID = 1L;
	
	public RunInputException(String message) {
		super(message);
	}
}
