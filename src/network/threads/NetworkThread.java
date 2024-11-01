package network.threads;

import java.net.DatagramSocket;

import io.consoleIO.ConsoleLogger;

public abstract class NetworkThread implements Runnable{
    protected DatagramSocket socket;
    protected volatile boolean running;
    
    protected NetworkThread(DatagramSocket socket) {
        running = true;
        this.socket = socket;
    }

    public Thread stop() {
        if (!running) return Thread.currentThread();
        
        running = false;
        ConsoleLogger.logRed("Stopping thread: " + this.getClass().getSimpleName());
        return Thread.currentThread();
    }
}
