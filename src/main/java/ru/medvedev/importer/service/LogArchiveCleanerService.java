package ru.medvedev.importer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class LogArchiveCleanerService {

    private static final String REG_EXP = "application\\.log\\.\\d{4}-%s-%s\\.\\d\\.gz";
    private static final int DAYS_LONG = 7;


    @Scheduled(cron = "${cron.logs-archive-delete}")
    public void cleanLogsArchive() {

        log.info("*** LAUNCH DELETE LOGS ARCHIVE");

        File newFile = new File("/opt/xlsx-importer/logs");
        LocalDate dateForDelete = LocalDate.now().minusDays(DAYS_LONG);
        String pattern = String.format(REG_EXP,
                dateForDelete.getMonthValue() < 10
                        ? "0" + dateForDelete.getMonthValue()
                        : String.valueOf(dateForDelete.getMonthValue()),
                dateForDelete.getDayOfMonth() < 10
                        ? "*" + dateForDelete.getDayOfMonth()
                        : String.valueOf(dateForDelete.getDayOfMonth()));

        Arrays.stream(newFile.listFiles())
                .forEach(file -> {
                    Pattern p = Pattern.compile(pattern);
                    Matcher m = p.matcher(file.getName());
                    if(m.matches()) {
                        log.info("*** DELETE " + file.getName());
                        file.delete();
                    }
                });
    }
}
