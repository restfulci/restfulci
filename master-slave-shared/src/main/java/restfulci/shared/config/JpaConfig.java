package restfulci.shared.config;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/*
 * To make downstream boot projects work, this configuration need to stay 
 * in the @ComponentScan path of "capsid". So it should be in here rather
 * than in test.
 *
 * TODO:
 * To understand why it is the case. Since "capsid" is a Spring Boot
 * application (for which this configuration is not needed, only
 * "DatabaseConfig" and "GitSourceConfig" is needed), I don't under why.
 */
@Configuration
@EnableJpaRepositories(basePackages="restfulci.shared.dao")
@EnableTransactionManagement
public class JpaConfig {

	@Autowired DataSource dataSource;
	
	@Bean
	public HibernateJpaVendorAdapter jpaVendorAdapter() {
		
		HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
		adapter.setDatabase(Database.POSTGRESQL);
		adapter.setShowSql(false);
		adapter.setGenerateDdl(true);
		adapter.setDatabasePlatform("org.hibernate.dialect.PostgreSQLDialect");
		
		return adapter;
	}
	
	@Bean
	public EntityManagerFactory entityManagerFactory() {
		
		LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
		entityManagerFactory.setDataSource(dataSource);
		entityManagerFactory.setPackagesToScan(new String[]{"restfulci.shared.dao", "restfulci.shared.domain"});
		entityManagerFactory.setJpaVendorAdapter(jpaVendorAdapter());
		
		/*
		 * TODO:
		 * May have duplicated setups in here as while as "jpaVendorAdapter()".
		 * Need to understand later what is needed and what is absolutely necessary.
		 * 
		 * TODO:
		 * Looks like there's no newer version of Hibernate Postgres Dialect beyond 9.5!?
		 * https://docs.jboss.org/hibernate/orm/current/javadocs/org/hibernate/dialect/package-summary.html
		 */
		Properties properties = new Properties();
		properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
		entityManagerFactory.setJpaProperties(properties);
		
		entityManagerFactory.afterPropertiesSet();
		return entityManagerFactory.getObject();
	}

	@Bean
	public PlatformTransactionManager transactionManager() {

		JpaTransactionManager txManager = new JpaTransactionManager();
		txManager.setEntityManagerFactory(entityManagerFactory());
		return txManager;
	}
}
