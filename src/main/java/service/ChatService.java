package service;

import service.connection.ClientConnection;

public class ChatService {

    private final ClientConnection player1;
    private final ClientConnection player2;

    public ChatService(ClientConnection player1, ClientConnection player2) {
        this.player1 = player1;
        this.player2 = player2;
    }


    public synchronized void route(
            ClientConnection sender,
            String username,
            String message){

        ClientConnection receiver =
                sender==player1 ? player2 : player1;

        receiver.sendMessage("[[CHAT]]"+username+": "+message);

    }
}