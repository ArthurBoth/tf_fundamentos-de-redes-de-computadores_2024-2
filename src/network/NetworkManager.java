package network;

import java.util.ArrayList;

import constants.ConfigurationConstants;
import io.IOManager;

public class NetworkManager {
    IOManager io;
    ArrayList<Route> routes;

    public NetworkManager() {
        io = new IOManager();
        routes = new ArrayList<>();
    }

    /**
     * Reads the file and sets up the routes table
     */
    public void start() {
        String[] defaultRoutes;

        defaultRoutes = io.getDefaultRoutes();
        for (String routeIp : defaultRoutes) {
            routes.add(Route.build()
                            .ip(routeIp)
                            .port(ConfigurationConstants.DEFAULT_PORT)
                            .weight(ConfigurationConstants.DEFAULT_WEIGHT));
        }
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

    public void exitNetwork() {
    }

    public void displayRoutes() {
    }
}
