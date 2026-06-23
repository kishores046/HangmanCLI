package model;

import java.net.Socket;

public class WaitingPlayer {
    private final Socket socket;
    private String username;

    public WaitingPlayer(Socket socket, String username) {
        this.socket = socket;
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Socket getSocket() {
        return socket;
    }

}
