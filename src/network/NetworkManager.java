package network;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.IOException;
import java.net.DatagramPacket;

import constants.ConfigurationConstants;
import io.IOManager;

public class NetworkManager implements Runnable {
    private static DatagramSocket client;
    private static Scanner sc;
    static IOManager io = new IOManager();
    static ArrayList<Route> routes = new ArrayList<>();

    public NetworkManager() {
        io = new IOManager();
        routes = new ArrayList<>();
    }

    public static void main(String[] args) throws IOException {
        // Reads the file and sets up the routes table
        String[] defaultRoutes;

        defaultRoutes = io.getDefaultRoutes();
        for (String routeIp : defaultRoutes) {
            routes.add(Route.build()
                    .ip(routeIp)
                    .port(ConfigurationConstants.DEFAULT_PORT)
                    .weight(ConfigurationConstants.DEFAULT_WEIGHT));
        }

        // Inicializacao do scanner
        sc = new Scanner(System.in);

        // Inicializacao da socket
        client = new DatagramSocket(9000, InetAddress.getByName("192.168.0.24"));
        System.out.println(client.getInetAddress());

        // IP da maquina
        InetAddress ipAddress = client.getInetAddress();
        // System.out.println(ipAddress);

        // Mensagem quando o roteador se conectar na rede
        byte[] sendData = new byte[1024];
        String msg = "*" + ipAddress.toString();
        sendData = msg.getBytes();
        for (Route r : routes) {
            InetAddress ipReceiver = InetAddress.getByName(r.getIp());
            int portReceiver = r.getPort();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipReceiver, portReceiver);
            client.send(sendPacket);
        }

        // Criacao das threads
        Thread t1 = new Thread(new NetworkManager()::routeAnnouncer);
        Thread t2 = new Thread(new NetworkManager()::messageReader);

        // Inicializacao das threads
        t1.start();
        t2.start();

        messageSender();
    }

    // Envio da tabela de roteamento
    public void routeAnnouncer() {
        System.out.println("AQUI 1");
        while (true) {
            // try {
            // // Espera de 15 segundos para envio da tabela de roteamento
            // wait(15000);

            // // Transformando a tabela de roteamento em mensagem
            // String msg = "";
            // for (Route r : routes) {
            // msg += "@" + r.getIp() + "-" + r.getWeight();
            // }

            // // Montando o pacote
            // byte[] sendData = new byte[1024];
            // sendData = msg.getBytes();
            // DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length);

            // // Envio do pacote
            // client.send(sendPacket);
            // } catch (InterruptedException e) {
            // e.printStackTrace();
            // } catch (IOException e) {
            // e.printStackTrace();
            // }
        }
    }

    // Leitor de mensagens
    public void messageReader() {
        System.out.println("AQUI 2");
        while (true) {
            // try {
            // // Recebimento da mensagem
            // byte[] receiveData = new byte[1024];
            // DatagramPacket receivePacket = new DatagramPacket(receiveData,
            // receiveData.length);
            // client.receive(receivePacket);
            // } catch (IOException e) {
            // e.printStackTrace();
            // }
        }
    }

    // Enviador de mensagens
    public static void messageSender() {
        while (true) {

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
    }
}
