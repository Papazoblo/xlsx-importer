package ru.medvedev.importer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@EnableFeignClients
public class XlsxImporterApplication {

    public static void main(String... args) {
        SpringApplication.run(XlsxImporterApplication.class);
    }
}
