package restfulci.pipeline.domain;

import lombok.Getter;

@Getter
public enum ReferredRunPhase {

	NOT_STARTED_YET('N', "Not started yet"),
	IN_PROGRESS('I', "In progress"),
	COMPLETE('C', "Complete"),
	ABORT('A', "Abort");
	
	private Character shortName;
	private String displayName;

	private ReferredRunPhase(Character shortName, String displayName) {
		this.shortName = shortName;
		this.displayName = displayName;
	}

	public static ReferredRunPhase fromShortName(Character shortName) {
		switch(shortName) {
		case 'N':
			return NOT_STARTED_YET;
		case 'I':
			return IN_PROGRESS;
		case 'C':
			return COMPLETE;
		case 'A':
			return ABORT;
		
		default:
			throw new IllegalArgumentException("Referred run phase shortName: "+shortName+" is not supported.");
		}
	}
}
