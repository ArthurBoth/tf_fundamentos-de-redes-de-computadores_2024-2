package network;

import java.util.ArrayList;

import constants.ConfigurationConstants;
import io.IOManager;
import network.threads.NetworkThread;
import network.threads.SenderThread;
import network.threads.TimeSchedulerThread;

public class NetworkManager implements Runnable {
    private IOManager io;
    private ArrayList<Route> routes;
    private NetworkThread senderThread;
    private NetworkThread receiverThread;
    private NetworkThread messageSchedulerThread;

    public NetworkManager() {
        io = new IOManager();
        routes = new ArrayList<>();
    }

    private void setup() {
        String[] defaultRoutes;

        defaultRoutes = io.getDefaultRoutes();
        for (String routeIp : defaultRoutes) {
            routes.add(Route.build()
                    .ip(routeIp)
                    .port(ConfigurationConstants.DEFAULT_PORT)
                    .weight(ConfigurationConstants.DEFAULT_WEIGHT));
        }

        senderThread = new SenderThread(ConfigurationConstants.DEFAULT_PORT);
        // receiverThread = new ReceiverThread(ConfigurationConstants.DEFAULT_PORT); // TODO
        messageSchedulerThread = TimeSchedulerThread.build()
                .port(ConfigurationConstants.DEFAULT_PORT)
                .defaultMessageTimeMS(ConfigurationConstants.DEFAULT_MESSAGE_TIME_MS)
                .messageSender((SenderThread) senderThread)
                .routesTableMessage(buildRouteAnnouncementMessage());

    }

    public void start() {
        setup();

        senderThread.run();
        receiverThread.run();
        messageSchedulerThread.run();
        
        // TODO: fazer IO em console para mensagens personalizadas 
    }

    /**
     * This method is called when:
     * - A destination IP not found in the routing table is received
     * - A destination IP with a lower weight is received
     * - A destination IP is no longer recieved
     */
    public void updateRoutes() {
    }

    /**
     * Logs the changes in the routing table in the console
     */
    private void displayChanges() {
    }

    private String buildRouteAnnouncementMessage() {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    public void exitNetwork() {
    }

    public void displayRoutes() {
    }

    @Override
    public void run() {
    }
}
