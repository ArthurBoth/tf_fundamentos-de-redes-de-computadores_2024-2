package network.threads;

import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import constants.ConfigurationConstants;
import constants.RegEx;
import io.consoleIO.ConsoleLogger;

public class TimeoutManagerThread extends NetworkThread {
    private BlockingQueue<String> messageQueue;
    private HashMap<String, Integer> ips; // <ipAdress, seconds>
    private Semaphore semaphore;

    public TimeoutManagerThread(DatagramSocket socket, BlockingQueue<String> messageQueue) {
        super(socket);
        this.messageQueue = messageQueue;
        ips = new HashMap<>();
        semaphore = new Semaphore(1);
    }

    @Override
    public void run() {
        super.running = true;
        while (running) {
            try {
                Thread.sleep(1000);
                count();
                verifyTimeout();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void resetCount(String ip) {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            return;
        }

        ips.put(ip, 0);

        semaphore.release();
    }

    public void setIps(Set<String> ipsToAdd) {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            return;
        }
        
        ips = ipsToAdd.stream()
                        .collect(Collectors.toMap(ip -> ip, _ -> 0, (ip, _) -> ip, HashMap::new));
        
        semaphore.release();

        if (ConfigurationConstants.DEBUG_LOGS) {
            StringBuilder builder = new StringBuilder();
            ips.forEach((key, _) -> builder.append(String.format("%s, ", key)));
            ConsoleLogger.logPurple(String.format("Set timeout: %s", 
                                        builder.toString().substring(0, builder.length() - 2)));
        }
    }

    public void removeIp(String ip) {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            return;
        }

        ips.remove(ip);

        semaphore.release();

        if (ConfigurationConstants.DEBUG_LOGS)
            ConsoleLogger.logPurple(String.format("Removed %s from timeouts", ip));
    }

    private void count() {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            return;
        }

        ips.replaceAll((_, value) -> value + 1);
     
        semaphore.release();

        if (ConfigurationConstants.DEBUG_LOGS)
            ConsoleLogger.logPurple("count++");
    }
    
    private void verifyTimeout() {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            return;
        }

        String[] timedOutIps = ips.entrySet()
                                    .stream()
                                    .filter(x -> ((x.getValue()) > ConfigurationConstants.TIMEOUT_TOLERANCE_SEC))
                                    .map(Entry::getKey)
                                    .toArray(String[]::new);
        
        semaphore.release();

        for (String ip : timedOutIps) {
            messageQueue.add(String.format("%s:¶ Timeout ip ¶:%s", RegEx.LOCALHOST, ip));

            if (ConfigurationConstants.DEBUG_LOGS)
                ConsoleLogger.logPurple(String.format("%s timed out", ip));
        }
    }
}
