import java.net.UnknownHostException;

import network.NetworkManager;

public class App {
    public static void main(String[] args) throws UnknownHostException {
        NetworkManager networkManager = new NetworkManager();
        networkManager.start();
    }
}
