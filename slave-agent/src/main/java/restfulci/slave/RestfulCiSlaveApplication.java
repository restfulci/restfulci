package restfulci.slave;

import java.util.function.Consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RestfulCiSlaveApplication {

	public static void main(String[] args) {
		SpringApplication.run(RestfulCiSlaveApplication.class, args);
	}
	
	@Bean
	public Consumer<String> executeTask() {
		return input -> {
			System.out.println("Received: " + input);
		};
	}
}
