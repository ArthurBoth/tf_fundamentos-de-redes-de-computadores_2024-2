package network.threads;

import java.net.DatagramSocket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicReferenceArray;

import io.consoleIO.ConsoleLogger;
import network.Route;

public class TimeSchedulerThread extends NetworkThread {
    private final long defaultMessageTimeMS;
    private final BlockingQueue<UDPWrapper> messageSender;

    private volatile String defaultMessage;
    private AtomicReferenceArray<Route> routes;

    public void setNeighbours(Route[] routes) {
        this.routes = new AtomicReferenceArray<>(routes);
    }

    public void setDefaultMessage(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    @Override
    public void run() {
        ConsoleLogger.logYellow("TimeScheduler thread started");
        super.running = true;

        while (running) {
            try {
                Thread.sleep(defaultMessageTimeMS);
                sendDefaultMessage();
            } catch (InterruptedException e) {
                ConsoleLogger.logError("Error while sleeping", e);
            }
        }
    }

    private void sendDefaultMessage() {
        Route route;
        for (int i = 0; i < routes.length(); i++) {
            route = routes.get(i);
            if (super.running) {
                messageSender.add(UDPWrapper.build()
                                            .ip(route.getSendToIp())
                                            .port(route.getPort())
                                            .message(defaultMessage));
            }
        }
    }

    // ************************************************************
    // Builder pattern implementation
    private TimeSchedulerThread(Builder builder) {
        super(builder.socket);
        this.defaultMessageTimeMS = builder.defaultMessageTimeMS;
        this.messageSender = builder.messageSender;
        this.defaultMessage = builder.defaultMessage;
        routes = new AtomicReferenceArray<>(new Route[0]);
    }

    public static SocketSetter build() {
        return new Builder();
    }

    public interface SocketSetter {
        DefaultMessageTimeMsSetter socket(DatagramSocket socket);
    }

    public interface DefaultMessageTimeMsSetter {
        MessageSenderSetter defaultMessageTimeMS(long defaultMessageTimeMS);
    }

    public interface MessageSenderSetter {
        DefaultMessageSetter messageSender(BlockingQueue<UDPWrapper> messageSender);
    }

    public interface DefaultMessageSetter {
        TimeSchedulerThread routesTableMessage(String defaultMessage);
    }

    private static class Builder
            implements SocketSetter, DefaultMessageTimeMsSetter, MessageSenderSetter, DefaultMessageSetter {
        private DatagramSocket socket;
        private long defaultMessageTimeMS;
        private BlockingQueue<UDPWrapper> messageSender;
        private String defaultMessage;

        @Override
        public DefaultMessageTimeMsSetter socket(DatagramSocket socket) {
            this.socket = socket;
            return this;
        }

        @Override
        public MessageSenderSetter defaultMessageTimeMS(long defaultMessageTimeMS) {
            this.defaultMessageTimeMS = defaultMessageTimeMS;
            return this;
        }

        @Override
        public DefaultMessageSetter messageSender(BlockingQueue<UDPWrapper> messageSender) {
            this.messageSender = messageSender;
            return this;
        }

        @Override
        public TimeSchedulerThread routesTableMessage(String defaultMessage) {
            this.defaultMessage = defaultMessage;
            return new TimeSchedulerThread(this);
        }
    }
}
