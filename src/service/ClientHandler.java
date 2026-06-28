package service;

import model.WaitingPlayer;
import util.LeaderboardPrinter;
import util.ProfilePrinter;

import java.io.*;
import java.net.Socket;
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

    public ClientHandler(Socket socket,
                         MatchMakingService matchMakingService,
                         ExecutorService gameSessionExecutor,
                         HangmanGameEngine hangmanGameEngine, LeaderboardPrinter leaderboardPrinter, MatchHistoryService matchHistoryDao, ProfilePrinter profilePrinter) {
        this.socket = socket;
        this.hangmanGameEngine=hangmanGameEngine;
        this.matchMakingService = matchMakingService;
        this.gameSessionExecutor = gameSessionExecutor;
        this.leaderboardPrinter = leaderboardPrinter;
        this.matchHistoryService =matchHistoryDao;
        this.profilePrinter=profilePrinter;

    }

    @Override
    public void run() {
        try {
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer =
                    new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);


            writer.println("INPUT_USERNAME");
            String username = reader.readLine();
            if (username == null || username.isBlank()) {
                writer.println("Invalid username. Disconnecting.");
                writer.println("Ended");
                return;
            }
            WaitingPlayer player = new WaitingPlayer(socket, username.trim(), -1);
            boolean authOk = authenticationService.handleAuth(player, username,reader, writer);
            if (!authOk) {
                writer.println("Ended");
                return;
            }

            writer.println("INPUT_MODE");
            writer.println("Choose mode");
            writer.println("1: Single Player");
            writer.println("2: Multi Player ");
            writer.println("3: LeaderBoard  ");
            writer.println("4: Match History");
            writer.println("5: Solo History");
            writer.println("6: Player Profile");
            writer.println("Enter your choice ");
            String choice = reader.readLine();
            if (choice == null) {
                logger.log(Level.WARNING, "Client disconnected before sending a choice");
                return;
            }

            switch (choice.trim()) {
                case "1" -> {

                    gameSessionExecutor.submit(new SingleModeSession(player,hangmanGameEngine,leaderboardPrinter, matchHistoryService));
                }
                case "2" -> {
                    matchMakingService.enqueue(player);
                }
                case "3" -> {
                    leaderboardPrinter.print(writer);
                    writer.println("Ended");
                }
                case "4" -> {
                    matchHistoryService.printMatchHistory(
                            player.getId(), player.getUsername(), writer);
                    writer.println("Ended");
                }
                case "5" -> {
                    matchHistoryService.printSinglePlayerHistory(
                            player.getId(), player.getUsername(), writer);
                    writer.println("Ended");
                }
                case "6"->{
                    profilePrinter.printPlayerProfile(player.getUsername(), writer);
                    writer.println("Ended");
                }
                default -> {
                    writer.println("Invalid choice. Disconnecting.");
                    writer.println("Ended");
                }
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "ClientHandler error", e);
        }
    }
}