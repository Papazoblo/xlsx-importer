package ru.medvedev.importer.component;

import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.apache.logging.log4j.util.Strings.isNotBlank;

@Data
@Scope("singleton")
@Component
public class XlsxStorage {

    private String fileName;
    private Long projectId;
    private Long fileId;

    public boolean isExist() {
        return isNotBlank(fileName);
    }
}
