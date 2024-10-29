package network;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import constants.ConfigurationConstants;
import io.IOManager;

public class NetworkManager implements Runnable {
    private DatagramSocket socket;
    private static Scanner sc;
    IOManager io;
    ArrayList<Route> routes;

    public NetworkManager(DatagramSocket s) {
        io = new IOManager();
        routes = new ArrayList<>();
    }

    public static void main(String[] args) throws SocketException, UnknownHostException {
        if (args.length < 2) {
            System.out.println("Usage: java Client <server_ip> <port>");
            return;
        }

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        sc = new Scanner(System.in);

        DatagramSocket client = new DatagramSocket();

        InetAddress ipAddress = InetAddress.getByName(hostname);

        Thread t = new Thread(new NetworkManager(client));
        t.start();

        // System.out.println(ipAddress);

        while (!client.isClosed()) {
            byte[] sendData = new byte[1024];
            String line = sc.nextLine();

        }
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

    @Override
    public void run() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'run'");
    }
}
