package restfulci.pipeline.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class DatabaseConfig {
	
	@Value("${POSTGRES_USER:foo}")
	private String postgresUser;
	
	@Value("${POSTGRES_PASSWORD:bar}")
	private String postgresPassword;

	@Profile("dev")
	@Bean
	public DataSource devDataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("org.postgresql.Driver");
		dataSource.setUrl("jdbc:postgresql://localhost:5433/restfulci-pipeline");
		dataSource.setUsername("postgres");
		dataSource.setPassword("postgres");
		return dataSource;
	}

	@Profile("docker")
	@Bean
	public DataSource dockerDataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("org.postgresql.Driver");
		dataSource.setUrl("jdbc:postgresql://pipeline-postgres:5432/restfulci-pipeline");
		dataSource.setUsername(postgresUser);
		dataSource.setPassword(postgresPassword);
		return dataSource;
	}
	
	@Profile("kubernetes")
	@Bean
	public DataSource kubernetesDataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("org.postgresql.Driver");
		dataSource.setUrl("jdbc:postgresql://restfulci-pipeline-postgres:5432/restfulci-pipeline");
		dataSource.setUsername(postgresUser);
		dataSource.setPassword(postgresPassword);
		return dataSource;
	}
	
	@Profile("circleci")
	@Bean
	public DataSource circleciDataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("org.postgresql.Driver");
		dataSource.setUrl("jdbc:postgresql://localhost:5432/restfulci");
		dataSource.setUsername("postgres");
		dataSource.setPassword("postgres");
		return dataSource;
	}
}
