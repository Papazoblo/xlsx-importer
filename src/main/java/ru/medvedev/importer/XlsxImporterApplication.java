package ru.medvedev.importer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"ru.medvedev.importer.*"})
@EnableFeignClients
@EnableScheduling
public class XlsxImporterApplication {

    public static void main(String... args) {
        SpringApplication.run(XlsxImporterApplication.class);
    }
}
