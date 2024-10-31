package network.threads;

import io.consoleIO.ConsoleLogger;
import network.Route;

import java.util.concurrent.atomic.AtomicReferenceArray;

public class TimeSchedulerThread extends NetworkThread{
    private final long defaultMessageTimeMS;
    private final SenderThread messageSender;
    
    private volatile String defaultMessage;
    private AtomicReferenceArray<Route> routes;

    public void setRoutes(Route[] routes) {
        this.routes = new AtomicReferenceArray<>(routes);
    }

    public void setDefaultMessage(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    @Override
    public void run() {
        while (!running) {
            try {
                Thread.sleep(defaultMessageTimeMS);
                sendDefaultMessage();
            } catch (InterruptedException e) {
                ConsoleLogger.logWhite("Message scheduler interrupted");
            }
        }
    }

    private void sendDefaultMessage() {
        for (int i = 0; i < routes.length(); i++) {
            Route route = routes.get(i);
            messageSender.sendMessage(route, defaultMessage);
        }
    }

    // ************************************************************
    // Builder pattern implementation
    private TimeSchedulerThread(Builder builder) {
        super(builder.port);
        this.defaultMessageTimeMS = builder.defaultMessageTimeMS;
        this.messageSender = builder.messageSender;
        this.defaultMessage = builder.defaultMessage;
        routes = new AtomicReferenceArray<>(new Route[0]);
    }

    public static PortSetter build() {
        return new Builder();
    }

    public interface PortSetter {
        DefaultMessageTimeMsSetter port(int port);
    }

    public interface DefaultMessageTimeMsSetter {
        MessageSenderSetter defaultMessageTimeMS(long defaultMessageTimeMS);
    }

    public interface MessageSenderSetter {
        DefaultMessageSetter messageSender(SenderThread messageSender);
    }

    public interface DefaultMessageSetter {
        TimeSchedulerThread routesTableMessage(String defaultMessage);
    }

    private static class Builder implements PortSetter, DefaultMessageTimeMsSetter, MessageSenderSetter, DefaultMessageSetter {
        private int port;
        private long defaultMessageTimeMS;
        private SenderThread messageSender;
        private String defaultMessage;

        @Override
        public DefaultMessageTimeMsSetter port(int port) {
            this.port = port;
            return this;
        }

        @Override
        public MessageSenderSetter defaultMessageTimeMS(long defaultMessageTimeMS) {
            this.defaultMessageTimeMS = defaultMessageTimeMS;
            return this;
        }

        @Override
        public DefaultMessageSetter messageSender(SenderThread messageSender) {
            this.messageSender = messageSender;
            return this;
        }

        @Override
        public TimeSchedulerThread routesTableMessage(String defaultMessage) {
            return new TimeSchedulerThread(this);
        }
    }
}
