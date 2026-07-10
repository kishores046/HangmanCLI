package service.connection;

import java.io.BufferedReader;
import java.io.PrintWriter;

public class ClientContext {

    private final PrintWriter out;
    private final BufferedReader in;

    public ClientContext(PrintWriter out, BufferedReader in) {
        this.out = out;
        this.in = in;
    }

    public BufferedReader getIn() {
        return in;
    }

    public PrintWriter getOut() {
        return out;
    }
}
