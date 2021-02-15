package restfulci.gateway.config.source;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KeycloakSource {

	private String issuerUri;
	private String jwtSetUri;
}
