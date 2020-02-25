package restfulci.slave;

import java.util.function.Consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

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
	
	/*
	 * TODO:
	 * 
	 * Cannot easily do this, because `RunBean` has subclasses, and it seems
	 * the JSON parser cannot successfully build subclasses. 
	 * 
	 * That's maybe fine/not needed, as we should probably define special DTO
	 * for communicating between master and slave, rather than use the exiting
	 * `RunBean`.
	 */
//	@Bean
//	public Consumer<RunBean> executeRun() {
//		return input -> {
//			System.out.println("Received job name: " + input.getJob().getName());
//			
//			ObjectMapper objectMapper = new ObjectMapper();
//			ObjectWriter objectWriter = objectMapper.writer().withDefaultPrettyPrinter();
//			try {
//				System.out.println("Received JSON: " + objectWriter.writeValueAsString(input));
//			} catch (JsonProcessingException e) {
//				
//			}
//		};
//	}
}
