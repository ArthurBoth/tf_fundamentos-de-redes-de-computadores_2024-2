package network;

public class Route {
    private final String sendToIp;
    private final int port;
    private final int weight;

    public boolean isNeighbor() {
        return this.getWeight() == 1;
    }

    // ************************************************************
    // Getters

    public int getPort() {
        return this.port;
    }

    public int getWeight() {
        return this.weight;
    }

    public String getSendToIp() {
        return sendToIp;
    }

    // ************************************************************
    // Builder pattern implementation
    private Route(Builder builder) {
        this.sendToIp = builder.sendToIp;
        this.port = builder.port;
        this.weight = builder.weight;
    }

    public static SendToIpSetter build() {
        return new Builder();
    }

    public interface SendToIpSetter {
        PortSetter sendTo(String sendToIp);
    }

    public interface PortSetter {
        WeightSetter port(int port);
    }

    public interface WeightSetter {
        Route weight(int weight);
    }

    private static class Builder implements SendToIpSetter, PortSetter, WeightSetter {
        private String sendToIp;
        private int port;
        private int weight;

        @Override
        public PortSetter sendTo(String sendToIp) {
            this.sendToIp = sendToIp;
            return this;
        }

        @Override
        public WeightSetter port(int port) {
            this.port = port;
            return this;
        }

        @Override
        public Route weight(int weight) {
            this.weight = weight;
            return new Route(this);
        }
    }

}