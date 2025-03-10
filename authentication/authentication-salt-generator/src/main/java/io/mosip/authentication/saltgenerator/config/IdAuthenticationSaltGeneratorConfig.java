package io.mosip.authentication.saltgenerator.config;

import static io.mosip.authentication.saltgenerator.constant.IdAuthenticationSaltGeneratorConstant.DATASOURCE_DRIVERCLASSNAME;
import static io.mosip.authentication.saltgenerator.constant.IdAuthenticationSaltGeneratorConstant.DATASOURCE_PASSWORD;
import static io.mosip.authentication.saltgenerator.constant.IdAuthenticationSaltGeneratorConstant.DATASOURCE_URL;
import static io.mosip.authentication.saltgenerator.constant.IdAuthenticationSaltGeneratorConstant.DATASOURCE_USERNAME;
import static io.mosip.authentication.saltgenerator.constant.IdAuthenticationSaltGeneratorConstant.DB_SCHEMA_NAME;
import static io.mosip.authentication.saltgenerator.constant.IdAuthenticationSaltGeneratorConstant.PACKAGE_TO_SCAN;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import io.mosip.authentication.saltgenerator.constant.IdAuthenticationSaltGeneratorConstant;
import io.mosip.kernel.core.util.StringUtils;

/**
 * The Class IdAuthenticationSaltGeneratorConfig - Provides configuration for Salt
 * generator application.
 *
 * @author Manoj SP
 */
@Configuration
@EnableTransactionManagement
public class IdAuthenticationSaltGeneratorConfig {
	
	/** The env. */
	@Autowired
	private Environment env;
	
	/** The naming resolver. */
	@Autowired
	private PhysicalNamingStrategyResolver namingResolver;
	
	/**
	 * Batch config.
	 *
	 * @return the batch configurer
	 */
	@Bean
	public BatchConfigurer batchConfig() {
		return new DefaultBatchConfigurer(null) {
			
			@Override
			public void setDataSource(DataSource dataSource) {
				// By default, Spring batch will try to create/update records in the provided
				// datasource related to Job completion, schedule etc.
				// This override will stop spring batch to create/update any tables in provided
				// Datasource and instead use Map based implementation internally.
			}
		};
	}
	
	/**
	 * Entity manager factory.
	 *
	 * @return the local container entity manager factory bean
	 */
	@Bean
	@Primary
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
		LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
		em.setDataSource(dataSource());
		em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
		em.setPackagesToScan(PACKAGE_TO_SCAN.getValue());
		em.setJpaPropertyMap(additionalProperties());
		return em;
	}
	
	/**
	 * Data source.
	 *
	 * @return the data source
	 */
	@Bean
	@Primary
	public DataSource dataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setUrl(env.getProperty("javax.persistence.jdbc.url"));
		dataSource.setUsername("idauser");
		dataSource.setPassword("Mosip@dev123");
		dataSource.setDriverClassName("org.postgresql.Driver");
		return dataSource;
	}
	
	/**
	 * Jpa transaction manager.
	 *
	 * @param emf the emf
	 * @return the jpa transaction manager
	 */
	@Bean
	@Primary
	public JpaTransactionManager jpaTransactionManager(EntityManagerFactory emf) {
		return new JpaTransactionManager(emf);
	}
	
	/**
	 * Additional properties.
	 *
	 * @return the map
	 */
	private Map<String, Object> additionalProperties() {
		Map<String, Object> jpaProperties = new HashMap<>();
		jpaProperties.put("hibernate.implicit_naming_strategy", SpringImplicitNamingStrategy.class.getName());
		jpaProperties.put("hibernate.physical_naming_strategy", namingResolver);
		jpaProperties.replace("hibernate.dialect", "org.hibernate.dialect.PostgreSQL92Dialect");
		return jpaProperties;
	}
	
}
