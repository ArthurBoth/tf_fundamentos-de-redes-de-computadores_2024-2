package network.threads;

import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.io.IOException;

import constants.ConfigurationConstants;
import io.consoleIO.ConsoleLogger;

public class SenderThread extends NetworkThread {
    BlockingQueue<UDPWrapper> messageQueue;

    public SenderThread(DatagramSocket socket, BlockingQueue<UDPWrapper> messageQueue) {
        super(socket);
        this.messageQueue = messageQueue;
    }

    private void sendMessage(UDPWrapper wrapper) {
        InetAddress neighbor;
        int port;
        byte[] data;
        DatagramPacket packet;

        try {
            if (ConfigurationConstants.DEBUG_LOGS) 
                ConsoleLogger.logCyan("Sending " + wrapper.getIp() + ":" + wrapper.getMessage());
                
            data = wrapper.getMessageBytes();
            neighbor = InetAddress.getByName(wrapper.getIp());
            port = wrapper.getPort();

            packet = new DatagramPacket(data, data.length, neighbor, port);
            socket.send(packet);

        } catch (SocketException e) {

        } catch (IOException e) {
            ConsoleLogger.logError("Error while sending", e);
        }
    }

    @Override
    public void run() {
        UDPWrapper message;
        super.running = true;
        
        while (running) {
            try {
                message = messageQueue.poll(ConfigurationConstants.SOCKET_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                if (message != null) sendMessage(message);
            } catch (InterruptedException e) {
                ConsoleLogger.logError("Error while sleeping", e);
            }
        }
    }
}
