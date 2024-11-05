package io.consoleIO;

import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;

public class TerminalManager implements Runnable {
    private final BlockingQueue<String> messageQueue;
    private final Scanner scanner;
    private final String userIp;

    private boolean running;
    
    public TerminalManager(String userIp, BlockingQueue<String> messageQueue) {
        this.userIp = userIp;
        this.messageQueue = messageQueue;
        scanner = new Scanner(System.in);
        running = true;
    }

    @Override
    public void run() {
        int userResponse;
        ConsoleLogger.logWhite("Starting console...");
        while (running) {
            printMenu();
            userResponse = getUserInputChoice();
            processResponse(userResponse);
        }
    }

    public Thread stop() {
        running = false;
        scanner.close();
        messageQueue.add("■KillMainThread");
        return Thread.currentThread();
    }

    private void printMenu() {
        ConsoleLogger.logCyan(String.format("You're currently %sconnected to the network", running ? "" : "not "));
        ConsoleLogger.logWhite("[1] Enter/Exit network");
        ConsoleLogger.logWhite("[2] Send custom message");
        ConsoleLogger.logWhite("[3] Kill this program");
        ConsoleLogger.logWhite("What would you like to do?");
        ConsoleLogger.logWhite(">> ", false);
    }

    private int getUserInputChoice() {
        int response = -1;
        while (running) {
            try {
                response = Integer.parseInt(scanner.nextLine());
                return response;
            } catch (NumberFormatException e) {
                ConsoleLogger.logRed("Invalid input. Please enter a number.");
            } catch (IllegalStateException e) {
                return - Integer.MAX_VALUE; // Scanner is closed
            }
        }
        return -1;
    }

    private void processResponse(int response) {
        ConsoleLogger.logYellow("DEBUG");
        switch (response) {
            case 1 -> {messageQueue.add("ºSwichNetworkState");}
            case 2 -> {buildMessage();} 
            case 3 -> {stop();}
            default -> {ConsoleLogger.logRed("Invalid response. Please try again.");}
        }
    }

    private void buildMessage() {
        String destinationIp = getUserInputIp();
        String message = getUserInputMessage();

        messageQueue.add(String.format("!%s;%s;%s", userIp, destinationIp, message));
    }

    private String getUserInputIp() {
        String ip = null;
        while (running) {
            try {
                ConsoleLogger.logWhite("Enter the Ip you want to send the message: ", false);
                ip = scanner.nextLine();
    
                if (ip.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) return ip;

                throw new NoSuchElementException();
            } catch (NoSuchElementException e) {
                ConsoleLogger.logRed("Invalid IP address. Please try again.");
            } catch (IllegalStateException e) {
                return null;
            }
        }
        return ip;
    }

    private String getUserInputMessage() {
        String message = null;
        while (running) {
            try {
                ConsoleLogger.logWhite("Enter the message you want to send: ", false);
                message = scanner.nextLine();
                
                if (message.length() > 0) return message;
                
            } catch (NoSuchElementException e) {
                ConsoleLogger.logRed("Invalid message. Please try again.");
            } catch (IllegalStateException e) {
                return null;
            }
        }
        return message;
    }
}
