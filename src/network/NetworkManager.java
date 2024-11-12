package network;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import constants.ConfigurationConstants;
import constants.RegEx;
import io.IOManager;
import io.consoleIO.ConsoleLogger;
import network.threads.ReceiverThread;
import network.threads.SenderThread;
import network.threads.TimeSchedulerThread;
import network.threads.TimeoutManagerThread;
import network.threads.UDPWrapper;

public class NetworkManager {
    // Variables relevant to the manager
    private IOManager io;
    private HashMap<String, Route> routes; // <destinationIp, <forwardIp, port, weight>>

    // Variables relevant to the network
    private DatagramSocket socket;
    private SenderThread senderThread;
    private ReceiverThread receiverThread;
    private TimeSchedulerThread messageSchedulerThread;
    private TimeoutManagerThread timeoutThread;
    private final String MY_IP_ADDRESS;

    // Variables relevant to the network state
    private BlockingQueue<String> receiveMessages;
    private BlockingQueue<UDPWrapper> sendMessages;
    private boolean insideNetwork;

    public NetworkManager() throws UnknownHostException{
        MY_IP_ADDRESS = InetAddress.getLocalHost().getHostAddress();
        routes = new HashMap<>();
        receiveMessages = new LinkedBlockingQueue<>();
        sendMessages = new LinkedBlockingQueue<>();

        io = new IOManager(receiveMessages, MY_IP_ADDRESS);
    }

    private void setup() {
        try {
            socket = new DatagramSocket(ConfigurationConstants.DEFAULT_PORT);
            sendMessages = new LinkedBlockingQueue<>();

            senderThread = new SenderThread(socket, sendMessages);
            receiverThread = new ReceiverThread(socket, receiveMessages);
            messageSchedulerThread = TimeSchedulerThread.build()
                    .socket(socket)
                    .defaultMessageTimeMS(ConfigurationConstants.DEFAULT_MESSAGE_TIME_MS)
                    .messageSender(sendMessages)
                    .routesTableMessage(RegEx.NULL);
            timeoutThread = new TimeoutManagerThread(socket, receiveMessages);

            updateNeighbors();
        } catch (IOException e) {
            ConsoleLogger.logError("Erro ao criar socket", e);
        }
    }

    private void buildDefaultRoutes() {
        String[] defaultRoutes;

        defaultRoutes = io.getDefaultRoutes();
        for (String routeIp : defaultRoutes) {
            routes.put(routeIp, Route.build()
                                        .sendTo(routeIp)
                                        .port(ConfigurationConstants.DEFAULT_PORT)
                                        .weight(ConfigurationConstants.DEFAULT_WEIGHT));
        }
    }

    public void start() {
        setup();
        io.startConsole(MY_IP_ADDRESS, insideNetwork);
        
        messageLoop();

        socket.close();
        messageSchedulerThread.stop().interrupt();
        senderThread.stop().interrupt();
        timeoutThread.stop().interrupt();
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
        if ((!senderIp.equals(RegEx.LOCALHOST)) && (!senderIp.equals(MY_IP_ADDRESS))) {
            timeoutThread.resetCount(senderIp);
        }

        switch (header) {
            case '@' -> {handleRoutesTable(senderIp, message);}
            case '*' -> {handleRouteAnnouncement(message);}
            case '!' -> {handleCustomMessage(message);}
            case 'º' -> {changeNetworkState();}        // Alt + 0186 
            case '¼' -> {handleSendMessage(message);}  // Alt + 7852 
            case '¶' -> {handleTimeout(message);}      // Alt + 7412
            case '▬' -> {return false;} // Should die  // Alt + 7958   
            default  -> {ConsoleLogger.logRed("Unsuported message header, message discarted: " + header);}
        }
        return true; // Should stay alive
    }

    private void handleRoutesTable(String senderIp, String message) {
        HashMap<String, Route> updates = new HashMap<>();
        String[] routesTable = message.split("@");
        String destinationIp;
        int weight;

        for (String route : routesTable) {
            if (route.isBlank() || route.equals(MY_IP_ADDRESS)) continue;

            destinationIp = route.split("-")[0];
            weight = Integer.parseInt(route.split("-")[1]);

            if (!routes.containsKey(destinationIp)) {
                updates.put(destinationIp, Route.build()
                                                .sendTo(senderIp)
                                                .port(ConfigurationConstants.DEFAULT_PORT)
                                                .weight(weight + 1));
            } else if (routes.get(destinationIp).getWeight() > weight + 1) {
                updates.put(destinationIp, Route.build()
                                                .sendTo(senderIp)
                                                .port(ConfigurationConstants.DEFAULT_PORT)
                                                .weight(weight + 1));
            } else {
                updates.put(destinationIp, routes.get(destinationIp));
            } 
        }

        for (Entry<String, Route> entry : routes.entrySet()) {
            if ((!updates.containsKey(entry.getKey())) && (!entry.getValue().getSendToIp().equals(senderIp))) {
                updates.put(entry.getKey(), entry.getValue());
            }
        }
        
        updates.put(senderIp, Route.build()
                                    .sendTo(senderIp)
                                    .port(ConfigurationConstants.DEFAULT_PORT)
                                    .weight(ConfigurationConstants.DEFAULT_WEIGHT));
        updateRoutesTable(updates);
    }

    private void handleRouteAnnouncement(String message) {
        String ip = message.substring(1);
        routes.put(ip, Route.build()
                            .sendTo(ip)
                            .port(ConfigurationConstants.DEFAULT_PORT)
                            .weight(ConfigurationConstants.DEFAULT_WEIGHT));

        updateNeighbors();
    }

    private void handleSendMessage(String message) {
        String[] messageData = message.split(";");
        String destinationIp = messageData[0].substring(2);

        if ((!ConfigurationConstants.SEND_LOOPBACK_MESSAGES) && 
            ((destinationIp.equals(RegEx.LOCALHOST)) || (destinationIp.equals(MY_IP_ADDRESS)))) 
            return;

        sendMessages.add(UDPWrapper.build()
                                    .ip(destinationIp)
                                    .port(ConfigurationConstants.DEFAULT_PORT)
                                    .message(message.substring(1)));
        if (ConfigurationConstants.DEBUG_LOGS) 
            ConsoleLogger.logYellow(String.format("Message sent to %s: %s", destinationIp, messageData[1]));
    }

    private void handleCustomMessage(String message) {
        String[] messageData = message.split(";");
        String destinationIp = messageData[1];

        if (destinationIp.equals(socket.getLocalAddress().getHostAddress())) {
            ConsoleLogger.logGreen(String.format("You have received a message from %s: %s", messageData[0], messageData[2]));
            return;
        }
         
        handleSendMessage(message); // forwards the message
    }

    private void handleTimeout(String message) {
        routes.entrySet().removeIf(x -> x.getValue().getSendToIp().equals(message.split(":")[1]));
        timeoutThread.removeIp(message.split(":")[1]);
        updateNeighbors();
    }

    private void updateRoutesTable(HashMap<String, Route> routeUpdates) {
        routeUpdates.entrySet().removeIf(x -> x.getValue().getWeight() > ConfigurationConstants.MAX_WEIGHT);
        routes = routeUpdates;

        updateNeighbors();
        messageSchedulerThread.sendDefaultMessage();
    }
    
    private void updateNeighbors() {
        messageSchedulerThread.setNeighbours(routes.values().stream().filter(Route::isNeighbor).toArray(Route[]::new));
        messageSchedulerThread.setDefaultMessage(buildRouteAnnouncementMessage());
        timeoutThread.setIps(new HashMap<>(routes).entrySet()
                                                    .stream()
                                                    .filter(x -> x.getValue().isNeighbor())
                                                    .map(Entry::getKey)
                                                    .collect(Collectors.toSet()));
    }

    private String buildRouteAnnouncementMessage() {
        StringBuilder message = new StringBuilder();
        for (Entry<String, Route> entry : routes.entrySet()) {
            message.append("@");
            message.append(entry.getKey());
            message.append("-");
            message.append(entry.getValue().getWeight());
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
        insideNetwork = false;

        senderThread.stop();
        timeoutThread.stop();
        receiverThread.stop();
        messageSchedulerThread.stop();
        io.exitNetwork();
    }

    private void enterNetwork() {
        insideNetwork = true;

        enterNetwork(new Thread(() -> senderThread.run()));
        enterNetwork(new Thread(() -> receiverThread.run()));
        enterNetwork(new Thread(() -> messageSchedulerThread.run()));
        enterNetwork(new Thread(() -> timeoutThread.run()));
        io.enterNetwork();

        buildDefaultRoutes();

        for (Route route : routes.values()) {
            if (route.isNeighbor()) {
                sendMessages.add(UDPWrapper.build()
                                            .ip(route.getSendToIp())
                                            .port(route.getPort())
                                            .message(String.format("*%s", MY_IP_ADDRESS)));
            }
        }
        
        messageSchedulerThread.sendDefaultMessage();

        // todo: review message's order
        // messageSchedulerThread.setNeighbours(routes.values().stream().filter(Route::isNeighbor).toArray(Route[]::new));
        // messageSchedulerThread.setDefaultMessage(buildRouteAnnouncementMessage());
        // io.logAction(String.format("Entered network with %d neighbors", routes.size()));
        // io.logAction(String.format("Routes: %s", buildRouteAnnouncementMessage()));
    }

    private void enterNetwork(Thread thread) {
        thread.start();
    }
}
