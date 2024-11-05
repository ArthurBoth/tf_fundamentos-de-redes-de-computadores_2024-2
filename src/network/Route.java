package network;

public class Route {
    private String ip;
    private int port;
    private int weight;

    public boolean isNeighbor() {
        return this.getWeight() == 1;
    } 

    // ************************************************************
    // Getters and Setters
    public String getIp() {
        return this.ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getWeight() {
        return this.weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    // ************************************************************
    // Builder pattern implementation
    private Route(Builder builder) {
        this.ip = builder.ip;
        this.port = builder.port;
        this.weight = builder.weight;
    }

    public static IpSetter build() {
        return new Builder();
    }

    public interface IpSetter {
        PortSetter ip(String ip);
    }

    public interface PortSetter {
        WeightSetter port(int port);
    }

    public interface WeightSetter {
        Route weight(int weight);
    }

    private static class Builder implements IpSetter, PortSetter, WeightSetter {
        private String ip;
        private int port;
        private int weight;

        @Override
        public PortSetter ip(String ip) {
            this.ip = ip;
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