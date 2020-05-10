package restfulci.slave;

import java.io.IOException;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import restfulci.shared.domain.RunMessageBean;
import restfulci.slave.service.DockerRunService;

@Slf4j
@SpringBootApplication(scanBasePackages= {"restfulci.slave", "restfulci.shared"})
public class SlaveApplication {
	
	@Autowired private DockerRunService dockerRunService;

	public static void main(String[] args) {
		SpringApplication.run(SlaveApplication.class, args);
	}
	
	@Bean
	public Consumer<String> executeRun() {
		return input -> {
			ObjectMapper objectMapper = new ObjectMapper();
			
			try {
				RunMessageBean runMessage = objectMapper.readValue(input, RunMessageBean.class);
				log.info("Receive message: "+runMessage);
				dockerRunService.runByMessage(runMessage);
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
			catch (IOException e) {
				e.printStackTrace();
			}
		};
	}
}
