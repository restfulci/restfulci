package restfulci.pipeline.domain;

import lombok.Getter;

@Getter
public enum ReferredRunStatus {

	NOT_STARTED_YET('N', "Not started yet"),
	IN_PROGRESS('I', "In progress"),
	SUCCESS('S', "Success"),
	FAIL('F', "Fail"),
	SKIP('K', "Skip"),
	ABORT('A', "Abort");
	
	private Character shortName;
	private String displayName;

	private ReferredRunStatus(Character shortName, String displayName) {
		this.shortName = shortName;
		this.displayName = displayName;
	}

	public static ReferredRunStatus fromShortName(Character shortName) {
		switch(shortName) {
		case 'N':
			return NOT_STARTED_YET;
		case 'I':
			return IN_PROGRESS;
		case 'S':
			return SUCCESS;
		case 'F':
			return FAIL;
		case 'K':
			return SKIP;
		case 'A':
			return ABORT;
		
		default:
			throw new IllegalArgumentException("Referred run status shortName: "+shortName+" is not supported.");
		}
	}
}
