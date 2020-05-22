package restfulci.pipeline.domain;

import lombok.Getter;

@Getter
public enum CycleStatus {

	IN_PROGRESS('I', "In progress"),
	SUCCESS('S', "Success"),
	FAIL('F', "Fail"),
	ABORT('A', "Abort");
	
	private Character shortName;
	private String displayName;

	private CycleStatus(Character shortName, String displayName) {
		this.shortName = shortName;
		this.displayName = displayName;
	}

	public static CycleStatus fromShortName(Character shortName) {
		switch(shortName) {
		case 'I':
			return IN_PROGRESS;
		case 'S':
			return SUCCESS;
		case 'F':
			return FAIL;
		case 'A':
			return ABORT;
		
		default:
			throw new IllegalArgumentException("Cycle status shortName: "+shortName+" is not supported.");
		}
	}
}
