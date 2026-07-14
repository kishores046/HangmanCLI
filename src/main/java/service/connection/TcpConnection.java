package service.connection;

import java.io.IOException;

public class TcpConnection implements ClientConnection{

    private final ClientContext clientContext;

    public TcpConnection(ClientContext clientContext) {
        this.clientContext = clientContext;
    }

    @Override
    public void sendMessage(String message) {
        clientContext.getOut().println(message);
    }

    @Override
    public String receiveMessage() throws IOException {
        return clientContext.getIn().readLine();
    }

    public ClientContext getClientContext() {
        return clientContext;
    }

    @Override
    public void sendFormatted(String format, Object... args) {
        clientContext.getOut().printf(format, args);
    }


}
