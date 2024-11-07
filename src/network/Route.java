package network;

public class Route {
    private String ipEnd;
    private int port;
    private int weight;
    private String ipTo;

    public boolean isNeighbor() {
        return this.getWeight() == 1;
    }

    // ************************************************************
    // Getters and Setters

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

    public String getIpEnd() {
        return ipEnd;
    }

    public void setIpEnd(String ipEnd) {
        this.ipEnd = ipEnd;
    }

    public String getIpTo() {
        return ipTo;
    }

    public void setIpTo(String ipTo) {
        this.ipTo = ipTo;
    }

    // ************************************************************
    // Builder pattern implementation
    private Route(Builder builder) {
        this.ipEnd = builder.ipEnd;
        this.port = builder.port;
        this.weight = builder.weight;
        this.ipTo = builder.ipTo;
    }

    public static IpEndSetter build() {
        return new Builder();
    }

    public interface IpEndSetter {
        PortSetter ipEnd(String ipEnd);
    }

    public interface PortSetter {
        WeightSetter port(int port);
    }

    public interface WeightSetter {
        IpToSetter weight(int weight);
    }

    public interface IpToSetter {
        Route ipTo(String ipTo);
    }

    private static class Builder implements IpEndSetter, PortSetter, WeightSetter, IpToSetter {
        private String ipEnd;
        private int port;
        private int weight;
        private String ipTo;

        @Override
        public PortSetter ipEnd(String ip) {
            ipEnd = ip;
            return this;
        }

        @Override
        public WeightSetter port(int port) {
            this.port = port;
            return this;
        }

        @Override
        public IpToSetter weight(int weight) {
            this.weight = weight;
            return this;
        }

        @Override
        public Route ipTo(String ip) {
            ipTo = ip;
            return new Route(this);
        }
    }

}