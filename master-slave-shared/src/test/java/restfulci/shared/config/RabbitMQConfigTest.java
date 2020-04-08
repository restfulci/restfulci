package restfulci.shared.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class RabbitMQConfigTest {
	
	/*
	 * No need to define those beans in RabbitMQConfig, as they are defined
	 * automatically by the library.
	 * 
	 * TODO:
	 * Then how can I 
	 * > rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
	 */
	@Autowired private AmqpAdmin admin;
	@Autowired private AmqpTemplate template;

	@Test
	public void testRabbitMQConnection() {

		admin.declareQueue(new Queue("rabbitmq-unittest-queue"));
		template.convertAndSend("rabbitmq-unittest-queue", "foo");
		assertEquals("foo", (String)template.receiveAndConvert("rabbitmq-unittest-queue"));
	}
}
