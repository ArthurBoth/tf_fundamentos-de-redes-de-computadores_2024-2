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

    public static void logGreen(String message) {
        System.out.print(Colours.GREEN);
        System.out.print(message);
        System.out.println(Colours.RESET);
    }

    public static void logWhite(String message) {
        System.out.print(Colours.RESET);
        System.out.println(message);
    }
    
    public static void logError(String message, Exception e) {
        System.out.println(Colours.RED);
        System.err.printf("ERROR: %s%n", message);

        System.out.print(Colours.YELLOW);
        e.printStackTrace();
        System.out.print(Colours.RESET);
    }

    public static void logError(String message) {
        System.out.print(Colours.RED);
        System.err.printf("ERROR: %s", message);
        System.out.println(Colours.RESET);
    }
}