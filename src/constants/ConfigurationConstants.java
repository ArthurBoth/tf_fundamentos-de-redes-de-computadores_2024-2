package constants;

public interface ConfigurationConstants {
    static final String CONFIG_FILE_PATH    = "configFiles/";
    static final String DEFAULT_ROUTES_FILE = "roteadores.txt";
    static final String TABLE_ROUTES_FILE   = "routes.csv";

    static final int MAX_WEIGHT       = 16;
    static final int DEFAULT_PORT     = 9000;
    static final int DEFAULT_WEIGHT   = 1;
    static final int MAX_MESSAGE_SIZE = 1024;
    
    static final long DEFAULT_MESSAGE_TIME_MS = 15 * 1000;
}
