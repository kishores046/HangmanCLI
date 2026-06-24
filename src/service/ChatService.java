package service;

import java.io.PrintWriter;

public class ChatService {

    private final PrintWriter out1;
    private final PrintWriter out2;

    public ChatService(PrintWriter out1, PrintWriter out2) {
        this.out1 = out1;
        this.out2 = out2;
    }

    public synchronized void route(PrintWriter senderOut, String username, String message) {
        PrintWriter recipientOut = (senderOut == out1) ? out2 : out1;
        recipientOut.println("[[CHAT]]" + username + ": " + message);
    }
}