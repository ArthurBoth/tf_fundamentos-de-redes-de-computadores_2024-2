package network.threads;

public class UDPWrapper {
    private String ip;
    private String message;
    private int port;

    // Gettes and setters
    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getMessage() {
        return message;
    }

    public byte[] getMessageBytes() {
        return message.getBytes();
    }

    public void setMessage(byte[] data) {
        this.message = new String(data);
    }
    
    //***************************************************************
    // Builder pattern implementation
    private UDPWrapper(Builder builder) {
        this.ip = builder.ip;
        this.port = builder.port;
        this.message = builder.message;
    }

    public static IpSetter build() {
        return new Builder();
    }

    public interface IpSetter {
        PortSetter ip(String ip);
    }

    public interface PortSetter {
        MessageSetter port(int port);
    }

    public interface MessageSetter {
        UDPWrapper message(String message);
    }

    private static class Builder implements IpSetter, PortSetter, MessageSetter {
        private String ip;
        private int port;
        private String message;

        @Override
        public PortSetter ip(String ip) {
            this.ip = ip;
            return this;
        }

        @Override
        public MessageSetter port(int port) {
            this.port = port;
            return this;
        }

        @Override
        public UDPWrapper message(String message) {
            this.message = message;
            return new UDPWrapper(this);
        }
    }
}
