package model;

import service.connection.ClientConnection;

import java.net.Socket;

public class WaitingPlayer {

    private final Socket socket;
    private String username;
    private int  id;
    private final ClientConnection connection;
    
    public WaitingPlayer(Socket socket, String username, int id, ClientConnection connection) {
        this.socket = socket;
        this.username = username;
        this.id=id;
        this.connection = connection;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ClientConnection getClientConnection() {
        return connection;
    }
}
