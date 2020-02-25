package restfulci.shared.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class DatabaseConfig {

	private Map<String, String> postgresUrlMap = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("local", "localhost");
			put("docker", "database");
		}
	};
	
	private DataSource getDataSource(String postgresUrl) {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("org.postgresql.Driver");
		dataSource.setUrl("jdbc:postgresql://"+postgresUrl+":5432/restfulci");
		dataSource.setUsername("postgres");
		dataSource.setPassword("postgres");
		return dataSource;
	}

	@Profile("local")
	@Bean
	public DataSource localDataSource() {
		return getDataSource(postgresUrlMap.get("local"));
	}

	@Profile("docker")
	@Bean
	public DataSource dockerDataSource() {
		return getDataSource(postgresUrlMap.get("docker"));
	}
}
