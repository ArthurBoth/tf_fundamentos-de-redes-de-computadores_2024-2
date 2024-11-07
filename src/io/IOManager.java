package io;

import java.util.concurrent.BlockingQueue;

import constants.ConfigurationConstants;
import constants.RegEx;
import io.consoleIO.TerminalManager;
import io.fileIO.FileIO;
import io.fileIO.FileLogger;

public class IOManager {
    BlockingQueue<String> networkQueue;
    TerminalManager terminal;
    FileLogger logger;

    public IOManager(BlockingQueue<String> networkQueue) {
        this.networkQueue = networkQueue;
        logger = new FileLogger();
    }
    
    public String[] getDefaultRoutes() {
        return FileIO.read(ConfigurationConstants.CONFIG_FILE_PATH +
                            ConfigurationConstants.ROUTES_FILE_NAME);
    }
    
    public void startConsole(String userIp, boolean insideNetwork) {
        terminal = new TerminalManager(userIp, networkQueue, insideNetwork);
        new Thread(() -> terminal.run()).start();
    }

    public void stopConsole() {
        terminal.stop().interrupt();
    }

    public void log(String senderIp,String message) {
        if ((!ConfigurationConstants.LOG_LOCALHOST) && (senderIp.equals(RegEx.LOCALHOST))) return;

        logger.log(String.format("Message from %s: %s", senderIp, message));
    }

    public void enterNetwork() {
        terminal.enterNetwork();
    }
    
    public void exitNetwork() {
        terminal.exitNetwork();
    }
}
