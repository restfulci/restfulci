package restfulci.slave;

import java.util.function.Consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages= {"restfulci.slave", "restfulci.shared"})
public class SlaveApplication {

	public static void main(String[] args) {
		SpringApplication.run(SlaveApplication.class, args);
	}
	
	@Bean
	public Consumer<String> executeRun() {
		return input -> {
			System.out.println("Received: " + input);
		};
	}
}
