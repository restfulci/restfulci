package restfulci.pipeline.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class DatabaseConfig {

	@Profile("dev")
	@Bean
	public DataSource devDataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("org.postgresql.Driver");
		dataSource.setUrl("jdbc:postgresql://localhost:5433/restfulci");
		dataSource.setUsername("postgres");
		dataSource.setPassword("postgres");
		return dataSource;
	}

//	@Profile("docker")
//	@Bean
//	public DataSource dockerDataSource() {
//		DriverManagerDataSource dataSource = new DriverManagerDataSource();
//		dataSource.setDriverClassName("org.postgresql.Driver");
//		dataSource.setUrl("jdbc:postgresql://postgres:5432/restfulci");
//		dataSource.setUsername("postgres");
//		dataSource.setPassword("postgres");
//		return dataSource;
//	}
//	
//	@Profile("kubernetes")
//	@Bean
//	public DataSource kubernetesDataSource() {
//		DriverManagerDataSource dataSource = new DriverManagerDataSource();
//		dataSource.setDriverClassName("org.postgresql.Driver");
//		dataSource.setUrl("jdbc:postgresql://restfulci-job-postgres:5432/restfulci");
//		dataSource.setUsername("postgres");
//		dataSource.setPassword("postgres");
//		return dataSource;
//	}
	
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
