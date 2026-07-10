package service;

import service.connection.ClientConnection;

import java.io.PrintWriter;

public class ClientDisconnectHandler {

    private  final ClientConnection clientConnection1;
    private final ClientConnection clientConnection2;
    private volatile boolean disconnected=false;

    public ClientDisconnectHandler(ClientConnection clientConnection1, ClientConnection clientConnection2) {
        this.clientConnection1 = clientConnection1;
        this.clientConnection2 = clientConnection2;
    }

    public boolean isDisconnected() {
        return disconnected;
    }


    public synchronized void handleClientDisconnect(ClientConnection disconnectedOut){
        if (disconnected) return;
        disconnected = true;
        ClientConnection recipientOut = (disconnectedOut == clientConnection1 ) ? clientConnection2 : clientConnection1;
        if (recipientOut == null) return;
        recipientOut.sendMessage("Opponent disconnected. You win by default.");
        recipientOut.sendMessage("Ended");
    }
}
