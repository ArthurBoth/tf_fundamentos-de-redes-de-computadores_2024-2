package network.threads;

import java.io.IOException;
import java.net.DatagramSocket;

import io.consoleIO.ConsoleLogger;

public abstract class NetworkThread implements Runnable{
    protected DatagramSocket socket;
    protected volatile boolean running;
    
    protected NetworkThread(int port) {
        try {
            running = true;
            socket = new DatagramSocket(port);
        } catch (IOException e) {
            ConsoleLogger.logError("Erro ao criar socket:", e);
        }
    }

    // TODO: Mover isto para uma classe própria
    /*
    public void messageReader() {
        byte[] data = new byte[ConfigurationConstants.MAX_MESSAGE_SIZE];
        DatagramPacket packet;
        String message;

        while (true) {
            try {
                DatagramPacket receivePacket = new DatagramPacket(data, data.length);
                socket.receive(receivePacket);

                message = new String(receivePacket.getData()).split(RegEx.NULL)[0];

                // Envia para a Thread principal

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    */
}
