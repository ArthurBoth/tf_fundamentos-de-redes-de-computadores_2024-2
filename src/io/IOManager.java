package io;

import java.util.Date;
import java.util.concurrent.BlockingQueue;

import constants.ConfigurationConstants;
import io.consoleIO.TerminalManager;
import io.fileIO.FileIO;

public class IOManager {
    BlockingQueue<String> networkQueue;
    TerminalManager terminal;

    public IOManager(BlockingQueue<String> networkQueue) {
        this.networkQueue = networkQueue;
        logMessage(String.format("New execution started at %s", new Date(System.currentTimeMillis()).toString()));
    }
    
    public String[] getDefaultRoutes() {
        return FileIO.read(ConfigurationConstants.CONFIG_FILE_PATH +
        ConfigurationConstants.DEFAULT_ROUTES_FILE);
    }
    
    public void startConsole(String userIp) {
        terminal = new TerminalManager(userIp, networkQueue);
        new Thread(() -> terminal.run()).start();
    }

    public void stopConsole() {
        terminal.stop().interrupt();
    }

    public void logMessage(String message) {
        FileIO.writeLine(ConfigurationConstants.DEFAULT_LOG_FILE, message);
    }
}
