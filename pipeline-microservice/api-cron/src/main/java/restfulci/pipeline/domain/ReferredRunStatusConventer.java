package restfulci.pipeline.domain;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class ReferredRunStatusConventer implements AttributeConverter<ReferredRunStatus,Character> {

	@Override
	public Character convertToDatabaseColumn(ReferredRunStatus status) {
		return status.getShortName();
	}
	
	@Override
	public ReferredRunStatus convertToEntityAttribute(Character dbData) {
		return ReferredRunStatus.fromShortName(dbData);
	}
}
