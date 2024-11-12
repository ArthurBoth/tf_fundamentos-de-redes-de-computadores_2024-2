package constants;

public interface ConfigurationConstants {
    static final String CONFIG_FILE_PATH    = "configFiles/";
    static final String ROUTES_FILE_NAME    = "roteadores.txt";
    static final String LOG_MESSAGES_FILE   = "messages.log";

    static final int DEFAULT_PORT          = 9000;
    static final int DEFAULT_WEIGHT        = 1;
    static final int MAX_MESSAGE_SIZE      = 1 << 20;
    static final int SOCKET_TIMEOUT_MS     = 1000;
    static final int TIMEOUT_TOLERANCE_SEC = 5;
    static final int MAX_WEIGHT            = 15;    
    
    static final long DEFAULT_MESSAGE_TIME_MS = 10 * 1000;

    static final boolean LOG_LOCALHOST          = true;
    static final boolean CLEAR_PREVIOUS_LOGS    = true;
    static final boolean SEND_LOOPBACK_MESSAGES = true;
    static final boolean DEBUG_LOGS             = true;     
}
