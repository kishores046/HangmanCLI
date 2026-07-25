package service;

import dao.PlayerStatsDAO;
import model.WaitingPlayer;
import service.connection.ClientConnection;
import util.HikariConnectionManager;
import util.PasswordUtil;

import javax.sql.DataSource;

public class AuthenticationService {



    private static final DataSource DATA_SOURCE=HikariConnectionManager.getDataSource();
    private final PlayerStatsDAO dao = new PlayerStatsDAO(DATA_SOURCE);
    private static final int MAX_AUTH_ATTEMPTS = 3;
    private static final java.util.regex.Pattern VALID_USERNAME =
            java.util.regex.Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final AuthenticationService INSTANCE = new AuthenticationService();
    public static AuthenticationService getInstance() { return INSTANCE; }

    private AuthenticationService() {}

    /**
     * New user   -> INPUT_PASSWORD_NEW  -> register
     * Old user   -> INPUT_PASSWORD_AUTH -> verify (up to MAX_AUTH_ATTEMPTS tries)
     * Returns true = proceed, false = disconnect.
     */
     public boolean handleAuth(WaitingPlayer waitingPlayer, String username, ClientConnection clientConnection)
            throws java.io.IOException {

         if (username == null || !VALID_USERNAME.matcher(username.trim()).matches()) {
             clientConnection.sendMessage("AUTH_BLOCKED");
             clientConnection.sendMessage("Invalid username. Use 3-20 letters, numbers, or underscores.");
             return false;
         }
         username = username.trim();

        if (!dao.usernameExists(username)) {
            clientConnection.sendMessage("INPUT_PASSWORD_NEW");
            clientConnection.sendMessage("Username '" + username + "' is available! Create a password:");

            String password = clientConnection.receiveMessage();
            if (password == null || password.isBlank()) {
                clientConnection.sendMessage("AUTH_BLOCKED");
                clientConnection.sendMessage("No password provided. Disconnecting.");
                return false;
            }
            int registeredId = dao.registerPlayer(username, PasswordUtil.hash(password.trim()));
            waitingPlayer.setId(registeredId);
            if (registeredId!=-1) {
                clientConnection.sendMessage("AUTH_SUCCESS");
                clientConnection.sendMessage("Account created! Welcome, " + username + "!");
                return true;
            } else {
                clientConnection.sendMessage("AUTH_BLOCKED");
                clientConnection.sendMessage("Username was just taken. Please reconnect with a different name.");
                return false;
            }
        } else {
            for (int attempt = 1; attempt <= MAX_AUTH_ATTEMPTS; attempt++) {
                clientConnection.sendMessage("INPUT_PASSWORD_AUTH");
                clientConnection.sendMessage("Welcome back, " + username + "! Enter your password ("
                        + attempt + "/" + MAX_AUTH_ATTEMPTS + "):");

                String password = clientConnection.receiveMessage();
                if (password == null) {
                    clientConnection.sendMessage("AUTH_BLOCKED");
                    clientConnection.sendMessage("Connection lost during authentication.");
                    return false;
                }

                int registerId=dao.authenticate(username, password.trim());
                waitingPlayer.setId(registerId);
                if (registerId!=-1) {
                    clientConnection.sendMessage("AUTH_SUCCESS");
                    clientConnection.sendMessage("Authenticated! Good to see you again, " + username + "!");
                    return true;
                }

                if (attempt < MAX_AUTH_ATTEMPTS) {
                    clientConnection.sendMessage("AUTH_FAILED");
                    clientConnection.sendMessage("Wrong password. " + (MAX_AUTH_ATTEMPTS - attempt) + " attempt(s) remaining.");
                }
            }

            clientConnection.sendMessage("AUTH_BLOCKED");
            clientConnection.sendMessage("Too many failed attempts. Disconnecting.");
            return false;
        }
    }

}
