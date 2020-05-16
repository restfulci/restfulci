package restfulci.pipeline.domain;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class ReferredRunPhaseConventer implements AttributeConverter<ReferredRunPhase,Character> {

	@Override
	public Character convertToDatabaseColumn(ReferredRunPhase status) {
		return status.getShortName();
	}
	
	@Override
	public ReferredRunPhase convertToEntityAttribute(Character dbData) {
		return ReferredRunPhase.fromShortName(dbData);
	}
}
