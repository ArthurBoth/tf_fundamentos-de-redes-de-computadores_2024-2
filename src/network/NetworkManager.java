package network;

import java.io.IOException;
import java.net.DatagramPacket;
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
    private HashMap<String, Route> routes; // <ipEnd, <ipEnd, port, weight, ipTo>>

    // Variables relevant to the network
    private DatagramSocket socket;
    private SenderThread senderThread;
    private ReceiverThread receiverThread;
    private TimeSchedulerThread messageSchedulerThread;
    private BlockingQueue<String> receiverQueue;
    private boolean insideNetwork;

    public NetworkManager() {
        io = new IOManager();
        routes = new HashMap<>();
        receiverQueue = new LinkedBlockingQueue<>();
    }

    private void setup() {
        String[] defaultRoutes;
        BlockingQueue<UDPWrapper> senderQueue;

        defaultRoutes = io.getDefaultRoutes();
        for (String routeIp : defaultRoutes) {
            ConsoleLogger.logWhite("Default route added: " + routeIp);
            routes.put(routeIp, Route.build()
                    .ipEnd(routeIp)
                    .port(ConfigurationConstants.DEFAULT_PORT)
                    .weight(ConfigurationConstants.DEFAULT_WEIGHT).ipTo(routeIp));
        }

        try {
            socket = new DatagramSocket(ConfigurationConstants.DEFAULT_PORT);
            senderQueue = new LinkedBlockingQueue<>();

            senderThread = new SenderThread(socket, senderQueue);
            receiverThread = new ReceiverThread(socket, receiverQueue);
            messageSchedulerThread = TimeSchedulerThread.build()
                    .socket(socket)
                    .defaultMessageTimeMS(ConfigurationConstants.DEFAULT_MESSAGE_TIME_MS)
                    .messageSender(senderQueue)
                    .routesTableMessage(buildRouteAnnouncementMessage());

            updateNeighbors();
        } catch (IOException e) {
            ConsoleLogger.logError("Erro ao criar socket", e);
        }
    }

    public void start() {
        setup();
        enterNetwork();

        // survive();
        messageLoop();
        // TODO: fazer IO em console para mensagens personalizadas
    }

    private void messageLoop() {
        messageSchedulerThread.setDefaultMessage("!qwertyuiop;127.0.0.1;Oi tudo bem?");
        boolean stayAlive = true;
        String receivedMessage;
        while (stayAlive) {
            try {
                ConsoleLogger.logGreen("aqui");
                receivedMessage = receiverQueue.poll(ConfigurationConstants.SOCKET_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                stayAlive = processMessage(receivedMessage);
            } catch (InterruptedException e) {
                ConsoleLogger.logError("Error while sleeping", e);
            }
        }
    }

    private boolean processMessage(String codedMessage) {
        if (codedMessage == null)
            return true;
        String senderIp = codedMessage.split(":", 2)[0];
        String message = codedMessage.split(":", 2)[1];
        char header = message.toCharArray()[0];

        switch (header) {
            case '@' -> {
                handleRoutesTable(message, senderIp);
            }
            case '*' -> {
                handleRouteAnnouncement(message, senderIp);
            }
            case '!' -> {
                handleCustomMessage(message);
            }
            case 'º' -> {
                changeNetworkState();
            } // Alt + 0186
            case '■' -> {
                return false;
            } // Should die // Alt + 1022
            case '↔' -> {
                return true;
            } // No message // Alt + 2589
            default -> {
                ConsoleLogger.logRed("Unsuported message header, message discarted: " + header);
            }
        }
        return true; // Should stay alive
    }

    private void handleRoutesTable(String table, String senderIp) {
        // Recebe tabela de rotas
        // Separa as rotas
        String[] routesTable; // array que vai guardar a string ip-peso
        String[] routeData; // array que vai guardar ip em [0] e peso em [1]
        Route[] updatedRoutes;

        routesTable = table.split("@");
        for (String route : routesTable) {
            routeData = route.split("-");
            String ipEnd = routeData[0];
            String weight = routeData[1];
            if (!routes.containsKey(ipEnd)) {
                routes.put(ipEnd,
                        Route.build().ipEnd(ipEnd).port(ConfigurationConstants.DEFAULT_PORT)
                                .weight((Integer.parseInt(weight) + 1)).ipTo(senderIp));
            } else {
                if (Integer.parseInt(weight) < routes.get(ipEnd).getWeight()) {
                    routes.replace(ipEnd,
                            Route.build().ipEnd(ipEnd).port(ConfigurationConstants.DEFAULT_PORT)
                                    .weight(Integer.parseInt(weight) + 1).ipTo(senderIp));
                    // PRINTAR A MODIFICACAO FEITA
                }
            }
            // if (routeData.length == 2) {
            // // Adiciona a rota à tabela de atualizações
            // this.routes.put(routeData[0], Route.build()
            // .ip(routeData[0])
            // .port(ConfigurationConstants.DEFAULT_PORT)
            // .weight(Integer.parseInt(routeData[1] + 1)));
        }
    }

    // updateRoutes(updatedRoutes);
    // }

    private void handleRouteAnnouncement(String message, String senderIp) {
        String ip = message.substring(1);
        routes.put(ip, Route.build()
                .ipEnd(ip)
                .port(ConfigurationConstants.DEFAULT_PORT)
                .weight(ConfigurationConstants.DEFAULT_WEIGHT).ipTo(senderIp));

        updateNeighbors();
    }

    private void handleCustomMessage(String message) {
        String[] messageSplited = message.split(";");
        String ipReceiver = messageSplited[1];

        // Ler a mensagem caso seja para mim
        if (ipReceiver == socket.getLocalAddress().getHostAddress()) {
            System.out.println(messageSplited[2]);

            // Senao, passo para o proximo
        } else {
            try {
                Route destinyRoute = routes.get(ipReceiver);
                InetAddress ipNeighbor = InetAddress.getByName(destinyRoute.getIpTo());
                byte[] sendData = new byte[ConfigurationConstants.MAX_MESSAGE_SIZE];
                sendData = message.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipNeighbor,
                        ConfigurationConstants.DEFAULT_PORT);
                socket.send(sendPacket);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
                    case 2 -> {
                    }
                    case 6 -> {
                        // changeNetworkState();
                    }
                    case 9 -> {
                        messageSchedulerThread.setDefaultMessage("New random message");
                    }
                    case 12 -> {
                        routes.put(InetAddress.getLocalHost().getHostAddress(),
                                Route.build()
                                        .ipEnd(InetAddress.getLocalHost().getHostAddress())
                                        .port(ConfigurationConstants.DEFAULT_PORT)
                                        .weight(ConfigurationConstants.DEFAULT_WEIGHT + 1).ipTo(null));
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
            message.append(route.getIpEnd());
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
