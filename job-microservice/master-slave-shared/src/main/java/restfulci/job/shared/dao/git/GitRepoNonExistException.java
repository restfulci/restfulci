package restfulci.job.shared.dao.git;

import java.io.IOException;

public class GitRepoNonExistException extends IOException {

	private static final long serialVersionUID = 1L;
	
	public GitRepoNonExistException(String message) {
		super(message);
	}
}
