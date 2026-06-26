package service;

import java.io.PrintWriter;

public class ClientDisconnectHandler {

    private final PrintWriter out1;
    private final PrintWriter out2;
    private volatile boolean disconnected=false;

    public boolean isDisconnected() {
        return disconnected;
    }

    public ClientDisconnectHandler(PrintWriter out1, PrintWriter out2) {
        this.out1 = out1;
        this.out2 = out2;
    }


    public synchronized void handleClientDisconnect(PrintWriter disconnectedOut){
        if (disconnected) return;
        disconnected = true;
        PrintWriter recipientOut = (disconnectedOut == out1) ? out2 : out1;
        if (recipientOut == null) return;
        recipientOut.println("Opponent disconnected. You win by default.");
        recipientOut.println("Ended");
    }
}
