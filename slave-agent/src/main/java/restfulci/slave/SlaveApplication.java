package restfulci.slave;

import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import restfulci.shared.domain.RunMessageBean;
import restfulci.slave.service.DockerRunService;

@SpringBootApplication(scanBasePackages= {"restfulci.slave", "restfulci.shared"})
public class SlaveApplication {
	
	@Autowired DockerRunService dockerRunService;

	public static void main(String[] args) {
		SpringApplication.run(SlaveApplication.class, args);
	}
	
	@Bean
	public Consumer<String> executeRun() {
		return input -> {
			ObjectMapper objectMapper = new ObjectMapper();
			
			try {
				RunMessageBean runMessage = objectMapper.readValue(input, RunMessageBean.class);
				dockerRunService.executeRun(runMessage);
			} 
			catch (JsonMappingException e) {
				e.printStackTrace();
			} 
			catch (JsonProcessingException e) {
				e.printStackTrace();
			} 
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		};
	}
}
