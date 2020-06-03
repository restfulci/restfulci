package restfulci.pipeline;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ApiCronApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiCronApplication.class, args);
	}
}
