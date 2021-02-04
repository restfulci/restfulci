package restfulci.job.shared.dao.git;

import java.io.IOException;

public class GitBranchNonExistException extends IOException {

	private static final long serialVersionUID = 1L;
	
	public GitBranchNonExistException(String message) {
		super(message);
	}
}
