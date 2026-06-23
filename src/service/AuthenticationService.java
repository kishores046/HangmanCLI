package service;

import dao.PlayerStatsDAO;
import util.PasswordUtil;

import java.io.BufferedReader;
import java.io.PrintWriter;

public class AuthenticationService {
    private final PlayerStatsDAO dao = new PlayerStatsDAO();
    private static final int MAX_AUTH_ATTEMPTS = 3;


    private static final AuthenticationService INSTANCE = new AuthenticationService();
    public static AuthenticationService getInstance() { return INSTANCE; }

    private AuthenticationService() {}

    /**
     * New user   -> INPUT_PASSWORD_NEW  -> register
     * Old user   -> INPUT_PASSWORD_AUTH -> verify (up to MAX_AUTH_ATTEMPTS tries)
     * Returns true = proceed, false = disconnect.
     */
     public boolean handleAuth(String username, BufferedReader in, PrintWriter out)
            throws java.io.IOException {

        if (!dao.usernameExists(username)) {
            out.println("INPUT_PASSWORD_NEW");
            out.println("Username '" + username + "' is available! Create a password:");

            String password = in.readLine();
            if (password == null || password.isBlank()) {
                out.println("AUTH_BLOCKED");
                out.println("No password provided. Disconnecting.");
                return false;
            }

            boolean registered = dao.registerPlayer(username, PasswordUtil.hash(password.trim()));
            if (registered) {
                out.println("AUTH_SUCCESS");
                out.println("Account created! Welcome, " + username + "!");
                return true;
            } else {
                out.println("AUTH_BLOCKED");
                out.println("Username was just taken. Please reconnect with a different name.");
                return false;
            }
        } else {
            for (int attempt = 1; attempt <= MAX_AUTH_ATTEMPTS; attempt++) {
                out.println("INPUT_PASSWORD_AUTH");
                out.println("Welcome back, " + username + "! Enter your password ("
                        + attempt + "/" + MAX_AUTH_ATTEMPTS + "):");

                String password = in.readLine();
                if (password == null) {
                    out.println("AUTH_BLOCKED");
                    out.println("Connection lost during authentication.");
                    return false;
                }

                if (dao.authenticate(username, password.trim())) {
                    out.println("AUTH_SUCCESS");
                    out.println("Authenticated! Good to see you again, " + username + "!");
                    return true;
                }

                if (attempt < MAX_AUTH_ATTEMPTS) {
                    out.println("AUTH_FAILED");
                    out.println("Wrong password. " + (MAX_AUTH_ATTEMPTS - attempt) + " attempt(s) remaining.");
                }
            }

            out.println("AUTH_BLOCKED");
            out.println("Too many failed attempts. Disconnecting.");
            return false;
        }
    }

}
