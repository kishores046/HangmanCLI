package service.connection;

import model.PlayerStats;

import java.io.IOException;

public interface ClientConnection {

    void sendMessage(String message);
    String receiveMessage() throws IOException;
    ClientContext getClientContext();
    void sendFormatted(String format,Object... args);


}
