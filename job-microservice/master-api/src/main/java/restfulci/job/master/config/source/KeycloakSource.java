package restfulci.job.master.config.source;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KeycloakSource {

	private String issuerUri;
	private String jwtSetUri;
}
