package network;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import constants.ConfigurationConstants;
import io.IOManager;
import io.consoleIO.ConsoleLogger;
import network.threads.ReceiverThread;
import network.threads.SenderThread;
import network.threads.TimeSchedulerThread;
import network.threads.UDPWrapper;

public class NetworkManager {
    // Variables relevant to the manager
    private IOManager io;
    private HashMap<String, Route> routes; // <destinationIp, <sendIp, port, weight>>

    // Variables relevant to the network
    private DatagramSocket socket;
    private SenderThread senderThread;
    private ReceiverThread receiverThread;
    private TimeSchedulerThread messageSchedulerThread;

    // Variables relevant to the network state
    private BlockingQueue<String> receiveMessages;
    private BlockingQueue<UDPWrapper> sendMessages;
    private boolean insideNetwork;

    public NetworkManager() {
        routes = new HashMap<>();
        receiveMessages = new LinkedBlockingQueue<>();
        sendMessages = new LinkedBlockingQueue<>();

        io = new IOManager(receiveMessages);
    }

    private void setup() {
        String[] defaultRoutes;

        defaultRoutes = io.getDefaultRoutes();
        for (String routeIp : defaultRoutes) {
            ConsoleLogger.logWhite("Default route added: " + routeIp);
            routes.put(routeIp, Route.build()
                                        .ip(routeIp)
                                        .port(ConfigurationConstants.DEFAULT_PORT)
                                        .weight(ConfigurationConstants.DEFAULT_WEIGHT));
        }

        try {
            socket = new DatagramSocket(ConfigurationConstants.DEFAULT_PORT);
            sendMessages = new LinkedBlockingQueue<>();

            senderThread = new SenderThread(socket, sendMessages);
            receiverThread = new ReceiverThread(socket, receiveMessages);
            messageSchedulerThread = TimeSchedulerThread.build()
                    .socket(socket)
                    .defaultMessageTimeMS(ConfigurationConstants.DEFAULT_MESSAGE_TIME_MS)
                    .messageSender(sendMessages)
                    .routesTableMessage(buildRouteAnnouncementMessage());
                    
            updateNeighbors();
        } catch (IOException e) {
            ConsoleLogger.logError("Erro ao criar socket", e);
        }
    }

    public void start() {
        try {
            setup();
            // enterNetwork();
            io.startConsole(InetAddress.getLocalHost().getHostAddress(), insideNetwork);
        } catch (UnknownHostException e) {
            ConsoleLogger.logError("Error while getting local host", e);
            return;
        }
        
        // survive();
        messageLoop();

        socket.close();
        messageSchedulerThread.stop().interrupt();
        senderThread.stop().interrupt();
        receiverThread.stop().interrupt();
        io.stopConsole();
    }
    
    private void messageLoop() {
        boolean stayAlive = true;
        String receivedMessage;
        while (stayAlive) {
            try {
                receivedMessage = receiveMessages.poll(ConfigurationConstants.SOCKET_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                stayAlive = processMessage(receivedMessage);
            } catch (InterruptedException e) {
                ConsoleLogger.logError("Error while sleeping", e);
            }
        }
    }

    private boolean processMessage(String codedMessage) {
        if (codedMessage == null) return true;

        String senderIp = codedMessage.split(":",2)[0];
        String message = codedMessage.split(":",2)[1];
        char header = message.toCharArray()[0];

        io.logMessage(senderIp, message);

        switch (header) {
            case '@' -> {handleRoutesTable(message);}
            case '*' -> {handleRouteAnnouncement(message);}
            case '!' -> {handleCustomMessage(message);}
            case 'º' -> {changeNetworkState();}                     // Alt + 0186 
            case '¼' -> {handleSendMessage(message.substring(1));}  // Alt + 7852  
            case '▬' -> {return false;} // Should die               // Alt + 7958   
            default  -> {ConsoleLogger.logRed("Unsuported message header, message discarted: " + header);}
        }
        return true; // Should stay alive
    }

    private void handleRoutesTable(String table) {
        // Recebe tabela de rotas
        // Separa as rotas
        String[] routesTable; // array que vai guardar a string ip-peso
        String[] routeData;   // array que vai guardar ip em [0] e peso em [1]
        Route[] updatedRoutes;

        routesTable = table.split("@");
        for (String route : routesTable) {
            routeData = route.split("-");
            if (routeData.length == 2) {
                // Adiciona a rota à tabela de atualizações
                this.routes.put(routeData[0], Route.build()
                                                    .ip(routeData[0])
                                                    .port(ConfigurationConstants.DEFAULT_PORT)
                                                    .weight(Integer.parseInt(routeData[1] + 1)));
            }
        }
        // updateRoutes(updatedRoutes);
    }

    private void handleRouteAnnouncement(String message) {
        String ip = message.substring(1);
        routes.put(ip, Route.build()
                            .ip(ip)
                            .port(ConfigurationConstants.DEFAULT_PORT)
                            .weight(ConfigurationConstants.DEFAULT_WEIGHT));
                            
        updateNeighbors();
    }
    
    private void handleCustomMessage(String message) {}

    private void handleSendMessage(String message) {
        String[] messageData = message.split(";");
        String destinationIp = messageData[0].substring(1);

        sendMessages.add(UDPWrapper.build()
                                    .ip(destinationIp)
                                    .port(ConfigurationConstants.DEFAULT_PORT)
                                    .message(message));
    }

    /* Test method */
    private void survive() { 
        boolean stayAlive = true;
        int i = 0;

        while (stayAlive) {
            try {
                Thread.sleep(1000);
                ConsoleLogger.logWhite("I'm alive");
                i++;
                switch (i) {
                    case 2 -> {changeNetworkState();}
                    case 6 -> {changeNetworkState();}
                    case 9 -> {messageSchedulerThread.setDefaultMessage("New random message");}
                    case 12 -> {
                                routes.put(InetAddress.getLocalHost().getHostAddress(),
                                            Route.build()
                                                .ip(InetAddress.getLocalHost().getHostAddress())
                                                .port(ConfigurationConstants.DEFAULT_PORT)
                                                .weight(ConfigurationConstants.DEFAULT_WEIGHT + 1));
                                updateNeighbors();
                            }
                    case 15 -> {
                        stayAlive = false;
                        ConsoleLogger.logWhite("I'm dying");
                    }
                }
            } catch (InterruptedException e) {
                ConsoleLogger.logError("Error while sleeping", e);
            } catch (IOException e) {
                ConsoleLogger.logError("Error while getting local host", e);
            }
        }
        
        socket.close();
        
        messageSchedulerThread.stop().interrupt();
        senderThread.stop().interrupt();
        receiverThread.stop().interrupt();
        
        ConsoleLogger.logWhite("I'm dea...");
    }
    /**
     * This method is called when:
     * - A destination IP not found in the routing table is received
     * - A destination IP with a lower weight is received
     * - A destination IP is no longer recieved
     */
    public void updateRoutes(Route[] routes) {
        // TODO implementar lógica de atualização de rotas

        updateNeighbors();
    }
    
    private void updateNeighbors() {
        messageSchedulerThread.setRoutes(routes.values().toArray(Route[]::new));
        messageSchedulerThread.setDefaultMessage(buildRouteAnnouncementMessage());
    }

    private String buildRouteAnnouncementMessage() {
        StringBuilder message = new StringBuilder();
        for (Route route : routes.values()) {
            message.append("@");
            message.append(route.getIp());
            message.append("-");
            message.append(route.getWeight());
        }

        return message.toString();
    }

    private void changeNetworkState() {
        if (insideNetwork) {
            exitNetwork();
        } else {
            enterNetwork();
        }
    }
    private void exitNetwork() {
        ConsoleLogger.logWhite("Exiting network");
        insideNetwork = false;

        senderThread.stop();
        receiverThread.stop();
        messageSchedulerThread.stop();
    }

    private void enterNetwork() {
        ConsoleLogger.logWhite("Entering network");
        insideNetwork = true;

        enterNetwork(new Thread(() -> senderThread.run()));
        enterNetwork(new Thread(() -> receiverThread.run()));
        enterNetwork(new Thread(() -> messageSchedulerThread.run()));
    }

    private void enterNetwork(Thread thread) {
        thread.start();
    }
}
