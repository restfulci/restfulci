package restfulci.job.shared.domain;

import lombok.Getter;

@Getter
public enum RunPhase {

	IN_PROGRESS('I', "In progress"),
	COMPLETE('C', "Complete"),
	ABORT('A', "Abort");
	
	private Character shortName;
	private String displayName;

	private RunPhase(Character shortName, String displayName) {
		this.shortName = shortName;
		this.displayName = displayName;
	}

	public static RunPhase fromShortName(Character shortName) {
		switch(shortName) {
		case 'I':
			return IN_PROGRESS;
		case 'C':
			return COMPLETE;
		case 'A':
			return ABORT;
		
		default:
			throw new IllegalArgumentException("Run phase shortName: "+shortName+" is not supported.");
		}
	}
}
