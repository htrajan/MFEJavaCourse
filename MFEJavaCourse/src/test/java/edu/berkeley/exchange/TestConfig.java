package edu.berkeley.exchange;

import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.H2;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages="edu.berkeley.exchange")
@ComponentScan(basePackages="edu.berkeley.exchange")
public class TestConfig 
{
	private static EmbeddedDatabase db;
	
	@Bean
	public DataSource dataSource()
	{
		if (db != null)
		{
			db.shutdown();
		}
		
		db = new EmbeddedDatabaseBuilder()
	     .setType(H2)
	     .setName("test")
	     .setScriptEncoding("UTF-8")
	     .ignoreFailedDrops(true)
	     .build();
		
		return db;
	}
	
	@Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() 
	{
		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		vendorAdapter.setGenerateDdl(true);

		LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
		factory.setJpaVendorAdapter(vendorAdapter);
		factory.setPackagesToScan("edu.berkeley.exchange");
		factory.setDataSource(dataSource());
		factory.afterPropertiesSet();

		return factory;
    }
 

    @Bean
    public JpaTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
        transactionManager.setDataSource(dataSource());
        return transactionManager;
    }
}
