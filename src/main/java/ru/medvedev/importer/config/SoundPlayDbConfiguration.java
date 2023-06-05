package ru.medvedev.importer.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(value = "ru.medvedev.importer.soundplay.repository",
        entityManagerFactoryRef = "soundPlayEntityManagerFactory",
        transactionManagerRef = "soundPlayTransactionManager")
public class SoundPlayDbConfiguration {

    @Value("${spring.datasource.play-sound-ds.username}")
    private String username;
    @Value("${spring.datasource.play-sound-ds.password}")
    private String password;
    @Value("${spring.datasource.play-sound-ds.jdbc-url}")
    private String url;

    @Bean
    @ConfigurationProperties("spring.datasource.play-sound-ds")
    public DataSource soundPlayDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "soundPlayTransactionManager")
    public JpaTransactionManager jpaTransactionManager(@Qualifier("soundPlayEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean(name = "soundPlayEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean(
            EntityManagerFactoryBuilder builder,
            @Qualifier("soundPlayDataSource") DataSource dataSource
    ) {
        return builder
                .dataSource(dataSource)
                .packages("ru.medvedev.importer.soundplay.entity")
                .persistenceUnit("soundPlay")
                .build();
    }

    @PostConstruct
    public void migrateFlyway() {
        Flyway.configure()
                .dataSource(url, username, password)
                .locations("classpath:db/migration/soundplay")
                .load()
                .migrate();
    }
}
