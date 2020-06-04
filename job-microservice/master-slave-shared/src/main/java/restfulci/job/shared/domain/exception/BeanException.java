package restfulci.job.shared.domain.exception;

import java.io.IOException;

public class BeanException extends IOException {

	private static final long serialVersionUID = 1L;
	
	public BeanException(String message) {
		super(message);
	}
}
