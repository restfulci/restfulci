package restfulci.pipeline.domain;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class CycleStatusConventer implements AttributeConverter<CycleStatus,Character> {

	@Override
	public Character convertToDatabaseColumn(CycleStatus status) {
		return status.getShortName();
	}
	
	@Override
	public CycleStatus convertToEntityAttribute(Character dbData) {
		return CycleStatus.fromShortName(dbData);
	}
}
