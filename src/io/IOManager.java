package io;

import java.util.concurrent.BlockingQueue;

import constants.ConfigurationConstants;
import constants.RegEx;
import io.consoleIO.ConsoleLogger;
import io.consoleIO.TerminalManager;
import io.fileIO.FileIO;
import io.fileIO.FileLogger;

public class IOManager {
    BlockingQueue<String> networkQueue;
    TerminalManager terminal;
    FileLogger msgLogger;

    public IOManager(BlockingQueue<String> networkQueue, String thisIp) {
        this.networkQueue = networkQueue;
        msgLogger = new FileLogger(ConfigurationConstants.LOG_MESSAGES_FILE);
        ConsoleLogger.logWhite(String.format("This machine's ip adress is: %s", thisIp));
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

    public void logMessage(String senderIp, String message) {
        if ((!ConfigurationConstants.LOG_LOCALHOST) && (senderIp.equals(RegEx.LOCALHOST))) return;

        msgLogger.log(String.format("Message from %s: %s", senderIp, message));
    }

    public void enterNetwork() {
        terminal.enterNetwork();
    }
    
    public void exitNetwork() {
        terminal.exitNetwork();
    }
}
