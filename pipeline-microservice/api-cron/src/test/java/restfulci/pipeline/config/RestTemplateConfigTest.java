package restfulci.pipeline.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@DisabledIfEnvironmentVariable(named="CI", matches="CircleCI")
public class RestTemplateConfigTest {
	
	@Autowired private RestTemplate restTemplate;

	@Test
	public void testRestTemplate() throws Exception {
		restTemplate.getForObject("/jobs/1/runs/1", Object.class);
	}
}
