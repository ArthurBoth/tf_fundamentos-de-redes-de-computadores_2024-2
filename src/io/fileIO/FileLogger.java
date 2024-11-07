package io.fileIO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import constants.ConfigurationConstants;

public class FileLogger {
    private final DateTimeFormatter dateTime;

    public FileLogger() {
        dateTime = DateTimeFormatter.ofPattern(ConfigurationConstants.DATE_TIME_LOG_FORMAT);
        FileIO.writeLine(ConfigurationConstants.DEFAULT_LOG_FILE, "*--".repeat(30) + '*');
    } 

    public void log(String message) {
        String line = String.format("%s: %s", dateTime.format(LocalDateTime.now()), message);
        FileIO.writeLine(ConfigurationConstants.DEFAULT_LOG_FILE, line);
    }
}
