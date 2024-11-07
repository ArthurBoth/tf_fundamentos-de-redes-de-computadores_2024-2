package network.threads;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;

import constants.ConfigurationConstants;
import constants.RegEx;
import io.consoleIO.ConsoleLogger;

public class ReceiverThread extends NetworkThread {
    BlockingQueue<String> messageQueue;

    public ReceiverThread(DatagramSocket socket, BlockingQueue<String> messageQueue) {
        super(socket);
        this.messageQueue = messageQueue;
    }

    @Override
    public void run() {
        ConsoleLogger.logBlue("Receiver thread started");
        byte[] data;
        DatagramPacket packet;
        String senderIp;
        String message;
        super.running = true;

        while (running) {
            try {
                data = new byte[ConfigurationConstants.MAX_MESSAGE_SIZE];
                packet = new DatagramPacket(data, data.length);
                socket.receive(packet);

                senderIp = packet.getAddress().getHostAddress();
                message = new String(packet.getData()).split(RegEx.NULL)[0];

                messageQueue.put(senderIp + ':' + message);

            } catch (SocketException e) {
                super.stop();
            } catch (IOException e) {
                ConsoleLogger.logError("Error while receiving message", e);
                ConsoleLogger.logCyan("Discarding message");
            } catch (InterruptedException e) {
                ConsoleLogger.logError("Error while putting message in queue", e);
            }
        }
    }
    
}
