package restfulci.shared.domain;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class RunPhaseConventer implements AttributeConverter<RunPhase,Character> {

	@Override
	public Character convertToDatabaseColumn(RunPhase status) {
		return status.getShortName();
	}
	
	@Override
	public RunPhase convertToEntityAttribute(Character dbData) {
		return RunPhase.fromShortName(dbData);
	}
}
