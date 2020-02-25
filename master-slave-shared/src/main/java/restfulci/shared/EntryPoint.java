package restfulci.shared;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EntityScan(basePackages = {"restfulci.shared"})
public class EntryPoint {

}
