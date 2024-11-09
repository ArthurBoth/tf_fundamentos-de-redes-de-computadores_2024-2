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

    public void setIps(Set<String> ipsToAdd) {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            return;
        }

        ips = ipsToAdd.stream()
                        .collect(Collectors.toMap(ip -> ip, _ -> 0, (ip, _) -> ip, HashMap::new));

        semaphore.release();
    }

    public void removeIp(String ip) {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            return;
        }

        ips.remove(ip);

        semaphore.release();
    }

    private void count() {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            return;
        }

        ips.replaceAll((_, value) -> value + 1);
     
        semaphore.release();
    }
    
    private void verifyTimeout() {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            return;
        }

        String[] timedOutIps = ips.entrySet()
                                    .stream()
                                    .filter(x -> ((x.getValue()) <= ConfigurationConstants.TIMEOUT_TOLERANCE_SEC))
                                    .map(Entry::getKey)
                                    .toArray(String[]::new);
        
        semaphore.release();

        for (String ip : timedOutIps) {
            messageQueue.add(String.format("%s:¶ Timeout ip ¶:%s", RegEx.LOCALHOST, ip));
        }
    }
}
