package client;

public class ServerDetails {

    private final String ipAddress;
    private final int port;

    public ServerDetails(String ipAddress, String port) {
        this.ipAddress = ipAddress;
        this.port = Integer.parseInt(port);
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }
}