package restfulci.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;

import restfulci.gateway.filters.pre.SimpleFilter;

@EnableZuulProxy
@SpringBootApplication(scanBasePackages= {"restfulci.gateway"})
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

	@Bean
		public SimpleFilter simpleFilter() {
			return new SimpleFilter();
		}
}
