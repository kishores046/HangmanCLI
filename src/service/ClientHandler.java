package service;

import model.WaitingPlayer;
import util.LeaderboardPrinter;

import javax.sql.DataSource;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final MatchMakingService matchMakingService;
    private final ExecutorService gameSessionExecutor;
    private final ExecutorService hangmanEngineExecutor;
    private final HangmanGameEngine hangmanGameEngine;
    private final LeaderboardPrinter leaderboardPrinter;

    private static final Logger logger = Logger.getLogger("ClientHandler");

    public ClientHandler(Socket socket,
                         MatchMakingService matchMakingService,
                         ExecutorService gameSessionExecutor,
                         ExecutorService hangmanEngineExecutor, HangmanGameEngine hangmanGameEngine, LeaderboardPrinter leaderboardPrinter) {
        this.socket = socket;
        this.hangmanGameEngine=hangmanGameEngine;
        this.matchMakingService = matchMakingService;
        this.gameSessionExecutor = gameSessionExecutor;
        this.hangmanEngineExecutor = hangmanEngineExecutor;
        this.leaderboardPrinter = leaderboardPrinter;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer =
                    new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            writer.println("INPUT_MODE");
            writer.println("Choose mode");
            writer.println("1: Single Player");
            writer.println("2: Multi Player ");
            writer.println("3: LeaderBoard  ");
            writer.println("Enter your choice ");

            String choice = reader.readLine();
            if (choice == null) {
                logger.log(Level.WARNING, "Client disconnected before sending a choice");
                return;
            }

            switch (choice.trim()) {
                case "1" -> {
                    WaitingPlayer player = new WaitingPlayer(socket, "");
                    gameSessionExecutor.submit(new SingleModeSession(player,hangmanGameEngine,leaderboardPrinter));
                }
                case "2" -> {
                    WaitingPlayer player =
                            new WaitingPlayer(socket, socket.getInetAddress().getHostName());
                    matchMakingService.enqueue(player);
                }
                case "3" -> {
                    leaderboardPrinter.print(writer);
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