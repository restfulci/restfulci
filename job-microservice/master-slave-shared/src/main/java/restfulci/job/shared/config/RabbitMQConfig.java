package restfulci.job.shared.config;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class RabbitMQConfig {
	
	@Value("${RABBITMQ_DEFAULT_USER:foo}")
	private String rabbitMQDefaultUser;
	
	@Value("${RABBITMQ_DEFAULT_PASS:bar}")
	private String rabbitMQDefaultPass;
	
	@Profile("dev")
	@Bean
	public CachingConnectionFactory devConnectionFactory() {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
		connectionFactory.setHost("localhost");
		connectionFactory.setPort(5673);
		connectionFactory.setUsername("restfulci");
		connectionFactory.setPassword("secretpassword");
		return connectionFactory;
	}
	
	@Profile("docker")
	@Bean
	public CachingConnectionFactory dockerConnectionFactory() {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
		connectionFactory.setHost("job-rabbitmq");
		connectionFactory.setPort(5672);
		connectionFactory.setUsername(rabbitMQDefaultUser);
		connectionFactory.setPassword(rabbitMQDefaultPass);
		return connectionFactory;
	}
	
	@Profile("kubernetes")
	@Bean
	public CachingConnectionFactory kubernetesConnectionFactory() {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
		connectionFactory.setHost("restfulci-job-rabbitmq");
		connectionFactory.setPort(5672);
		connectionFactory.setUsername(rabbitMQDefaultUser);
		connectionFactory.setPassword(rabbitMQDefaultPass);
		return connectionFactory;
	}
	
	@Profile("circleci")
	@Bean
	public CachingConnectionFactory circleciConnectionFactory() {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
		connectionFactory.setHost("localhost");
		connectionFactory.setPort(5672);
		connectionFactory.setUsername("guest");
		connectionFactory.setPassword("guest");
		return connectionFactory;
	}
}