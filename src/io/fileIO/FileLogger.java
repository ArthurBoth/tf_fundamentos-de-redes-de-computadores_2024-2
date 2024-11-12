package io.fileIO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import constants.ConfigurationConstants;
import constants.RegEx;

public class FileLogger {
    private final String filepath;
    private final DateTimeFormatter dateTime;

    public FileLogger(String filepath) {
        this.filepath = filepath;
        dateTime = DateTimeFormatter.ofPattern(RegEx.DATE_TIME_LOG_FORMAT);
        
        if (ConfigurationConstants.CLEAR_PREVIOUS_LOGS) FileIO.clearFile(filepath);
  
        FileIO.writeLine(filepath, "*--".repeat(30) + '*');
    } 

    public void log(String message) {
        String line = String.format("%s: %s", dateTime.format(LocalDateTime.now()), message);
        FileIO.writeLine(filepath, line);
    }
}
