package restfulci.job.shared.domain;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class RunStatusConventer implements AttributeConverter<RunStatus,Character> {

	@Override
	public Character convertToDatabaseColumn(RunStatus status) {
		return status.getShortName();
	}
	
	@Override
	public RunStatus convertToEntityAttribute(Character dbData) {
		return RunStatus.fromShortName(dbData);
	}
}
