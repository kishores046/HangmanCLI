package service;

import model.WaitingPlayer;
import service.connection.ClientConnection;
import service.connection.ClientContext;
import util.LeaderboardPrinter;
import util.ProfilePrinter;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final MatchMakingService matchMakingService;
    private final ExecutorService gameSessionExecutor;
    private final HangmanGameEngine hangmanGameEngine;
    private final LeaderboardPrinter leaderboardPrinter;
    private final MatchHistoryService matchHistoryService;
    private static final AuthenticationService authenticationService=AuthenticationService.getInstance();
    private final ProfilePrinter profilePrinter;
    private static final Logger logger = Logger.getLogger("ClientHandler");
    private final ClientConnection clientConnection;
    public ClientHandler(Socket socket,
                         MatchMakingService matchMakingService,
                         ExecutorService gameSessionExecutor,
                         HangmanGameEngine hangmanGameEngine, LeaderboardPrinter leaderboardPrinter, MatchHistoryService matchHistoryDao, ProfilePrinter profilePrinter, ClientConnection clientConnection) {
        this.socket = socket;
        this.hangmanGameEngine=hangmanGameEngine;
        this.matchMakingService = matchMakingService;
        this.gameSessionExecutor = gameSessionExecutor;
        this.leaderboardPrinter = leaderboardPrinter;
        this.matchHistoryService =matchHistoryDao;
        this.profilePrinter=profilePrinter;
        this.clientConnection = clientConnection;
    }

    @Override
    public void run() {
        try {
            clientConnection.sendMessage("INPUT_USERNAME");
            String username = clientConnection.receiveMessage();
            if (username == null || username.isBlank()) {
                clientConnection.sendMessage("Invalid username. Disconnecting.");
                clientConnection.sendMessage("Ended");
                return;
            }
            WaitingPlayer player = new WaitingPlayer(socket,username.trim(), -1,clientConnection);
            boolean authOk = authenticationService.handleAuth(player, username,clientConnection);
            if (!authOk) {
                clientConnection.sendMessage("Ended");
                return;
            }
            clientConnection.sendMessage("INPUT_MODE");
            clientConnection.sendMessage("Choose mode");
            clientConnection.sendMessage("1: Single Player");
            clientConnection.sendMessage("2: Multi Player ");
            clientConnection.sendMessage("3: LeaderBoard  ");
            clientConnection.sendMessage("4: Match History");
            clientConnection.sendMessage("5: Solo History");
            clientConnection.sendMessage("6: Player Profile");
            clientConnection.sendMessage("Enter your choice ");
            String choice = clientConnection.receiveMessage();
            if (choice == null) {
                logger.log(Level.WARNING, "Client disconnected before sending a choice");
                return;
            }

            switch (choice.trim()) {
                case "1" -> {
                    gameSessionExecutor.submit(
                            new SingleModeSession(player, hangmanGameEngine,
                                    leaderboardPrinter, matchHistoryService,
                                    clientConnection));
                }
                case "2" -> {
                    matchMakingService.enqueue(player,clientConnection);
                }
                case "3" -> {
                    leaderboardPrinter.print(clientConnection);
                    clientConnection.sendMessage("Ended");
                }
                case "4" -> {
                    matchHistoryService.printMatchHistory(
                            player.getId(), player.getUsername(), clientConnection);
                    clientConnection.sendMessage("Ended");
                }
                case "5" -> {
                    matchHistoryService.printSinglePlayerHistory(
                            player.getId(), player.getUsername(), clientConnection);
                    clientConnection.sendMessage("Ended");
                }
                case "6"->{
                    profilePrinter.printPlayerProfile(player.getUsername(), clientConnection);
                    clientConnection.sendMessage("Ended");
                }
                default -> {
                    clientConnection.sendMessage("Invalid choice. Disconnecting.");
                    clientConnection.sendMessage("Ended");
                }
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, "ClientHandler error", e);
        }
    }
}