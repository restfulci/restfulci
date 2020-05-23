package restfulci.job.shared.domain;

import lombok.Getter;

@Getter
public enum RunStatus {

	IN_PROGRESS('I', "In progress"),
	SUCCEED('S', "Succeed"),
	FAIL('F', "Fail"),
	ABORT('A', "Abort");
	
	private Character shortName;
	private String displayName;

	private RunStatus(Character shortName, String displayName) {
		this.shortName = shortName;
		this.displayName = displayName;
	}

	public static RunStatus fromShortName(Character shortName) {
		switch(shortName) {
		case 'I':
			return IN_PROGRESS;
		case 'S':
			return SUCCEED;
		case 'F':
			return FAIL;
		case 'A':
			return ABORT;
		
		default:
			throw new IllegalArgumentException("Run status shortName: "+shortName+" is not supported.");
		}
	}
}
