package restfulci.shared.config;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class RabbitMQConfig {
	
	@Profile("dev")
	@Bean
	public CachingConnectionFactory devConnectionFactory() {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
		connectionFactory.setHost("localhost");
		connectionFactory.setPort(5673);
		connectionFactory.setUsername("guest");
		connectionFactory.setPassword("guest");
		return connectionFactory;
	}
	
	@Profile("docker")
	@Bean
	public CachingConnectionFactory dockerConnectionFactory() {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
		connectionFactory.setHost("rabbitmq");
		connectionFactory.setPort(5672);
		connectionFactory.setUsername("guest");
		connectionFactory.setPassword("guest");
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
	
	/*
	 * Error in E2E test (although test passed without problem):
	 * 
slave-agent_1  | 2020-04-17 03:49:50.881 ERROR 1 --- [in-0.runqueue-2] o.s.integration.handler.LoggingHandler   : org.springframework.messaging.MessageHandlingException: error occurred during processing message in 'MethodInvokingMessageProcessor' [org.springframework.integration.handler.MethodInvokingMessageProcessor@2a7b33a7]; nested exception is org.zeroturnaround.zip.ZipException: Given directory '/tmp/result6040049793107649164' doesn't contain any files!, failedMessage=GenericMessage [payload=byte[34], headers={amqp_receivedDeliveryMode=PERSISTENT, amqp_receivedExchange=, amqp_deliveryTag=20, deliveryAttempt=3, amqp_consumerQueue=executeRun-in-0.runqueue, amqp_redelivered=false, amqp_receivedRoutingKey=executeRun-in-0.runqueue, amqp_contentEncoding=UTF-8, id=475ae8af-7aa2-a019-756f-c34b114f1a9d, amqp_consumerTag=amq.ctag-u0ZyW_nLUiIoNQuSejHeSA, sourceData=(Body:'{
slave-agent_1  |   "jobId" : 20,
slave-agent_1  |   "runId" : 20
slave-agent_1  | }' MessageProperties [headers={}, contentType=text/plain, contentEncoding=UTF-8, contentLength=0, receivedDeliveryMode=PERSISTENT, priority=0, redelivered=false, receivedExchange=, receivedRoutingKey=executeRun-in-0.runqueue, deliveryTag=20, consumerTag=amq.ctag-u0ZyW_nLUiIoNQuSejHeSA, consumerQueue=executeRun-in-0.runqueue]), contentType=text/plain, timestamp=1587095365849}]
	 *
	 * Sounds like a message has been delivered multiple times, and the last time
	 * the job has been deleted, so `runRepository.findById(runMessage.getRunId()).get()`
	 * gives 
	 * > java.util.NoSuchElementException: No value present
	 * 
	 * TODO:
	 * Should acknowledge properly after finishing consuming it, and/or change
	 * the re-deliver time, so the message is not delivered >1 times. 
	 */
}