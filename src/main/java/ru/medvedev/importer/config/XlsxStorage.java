package ru.medvedev.importer.config;

import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.apache.logging.log4j.util.Strings.isNotBlank;

@Data
@Scope("singleton")
@Component
public class XlsxStorage {

    private String fileName;

    public boolean isExist() {
        return isNotBlank(fileName);
    }
}
