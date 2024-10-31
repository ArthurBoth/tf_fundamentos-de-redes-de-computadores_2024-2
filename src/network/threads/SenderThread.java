package network.threads;

import java.net.InetAddress;
import java.net.DatagramPacket;
import java.io.IOException;

import network.Route;

public class SenderThread extends NetworkThread {
    public SenderThread(int port) {
        super(port);
    }

    public void sendMessage(Route route, String message) {
        InetAddress neighbor;
        int port;
        byte[] data;
        DatagramPacket packet;

        try {
            data = message.getBytes();
            neighbor = InetAddress.getByName(route.getIp());
            port = route.getPort();

            packet = new DatagramPacket(data, data.length, neighbor, port);
            socket.send(packet);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
