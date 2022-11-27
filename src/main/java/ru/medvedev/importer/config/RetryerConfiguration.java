package ru.medvedev.importer.config;

import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RetryerConfiguration {

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(60000, 60000, 2);
    }
}
