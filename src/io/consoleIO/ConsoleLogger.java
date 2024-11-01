package io.consoleIO;

public abstract class ConsoleLogger {

    @SuppressWarnings("unused")
    private abstract class Colours {
        public static final String RESET    = "\u001B[0m";
        public static final String BLACK    = "\u001B[30m";
        public static final String RED      = "\u001B[31m";
        public static final String GREEN    = "\u001B[32m";
        public static final String YELLOW   = "\u001B[33m";
        public static final String BLUE     = "\u001B[34m";
        public static final String PURPLE   = "\u001B[35m";
        public static final String CYAN     = "\u001B[36m";
        public static final String WHITE    = "\u001B[37m";
    }

    public static synchronized void logGreen(String message) {
        System.out.print(Colours.GREEN);
        System.out.print(message);
        System.out.println(Colours.RESET);
    }
    
    public static synchronized void logBlue(String message) {
        System.out.print(Colours.BLUE);
        System.out.print(message);
        System.out.println(Colours.RESET);
    }

    public static synchronized void logCyan(String message) {
        System.out.print(Colours.CYAN);
        System.out.print(message);
        System.out.println(Colours.RESET);
    }

    public static synchronized void logYellow(String message) {
        System.out.print(Colours.YELLOW);
        System.out.print(message);
        System.out.println(Colours.RESET);
    }

    public static synchronized void logWhite(String message) {
        System.out.print(Colours.RESET);
        System.out.println(message);
    }

    public static synchronized void logRed(String message) {
        System.out.print(Colours.RED);
        System.out.println(message);
        System.out.print(Colours.RESET);
    }
    
    public static synchronized void logError(String message, Exception e) {
        System.out.println(Colours.RED);
        System.err.printf("ERROR: %s%n", message);

        System.out.print(Colours.YELLOW);
        e.printStackTrace();
        System.out.print(Colours.RESET);
    }

    public static synchronized void logError(String message) {
        System.out.print(Colours.RED);
        System.err.printf("ERROR: %s", message);
        System.out.println(Colours.RESET);
    }
}