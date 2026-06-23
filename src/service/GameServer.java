package service;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameServer {

    private static final ExecutorService CLIENT_HANDLER_POOL =
            Executors.newFixedThreadPool(10);

   private static final ExecutorService GAME_SESSION_POOL =
            Executors.newFixedThreadPool(20);

     private static final ExecutorService HANGMAN_ENGINE_POOL =
            Executors.newCachedThreadPool();

    private static final MatchMakingService MATCHMAKER =
            new MatchMakingService(GAME_SESSION_POOL, HANGMAN_ENGINE_POOL);

    private static final Logger serverLogger = Logger.getLogger("GameServer");

    public static void main(String[] args) {
         try (ServerSocket serverSocket = new ServerSocket()) {
            SocketAddress address = new InetSocketAddress(8080);
            serverSocket.bind(address);
            serverLogger.log(Level.INFO, "Hangman server running on port 8080");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                serverLogger.log(Level.INFO, "Client connected: {0}", clientSocket.getInetAddress().getHostAddress());
                CLIENT_HANDLER_POOL.execute(
                        new ClientHandler(clientSocket, MATCHMAKER, GAME_SESSION_POOL, HANGMAN_ENGINE_POOL));
            }

        } catch (Exception e) {

            serverLogger.log(Level.SEVERE, "Server crashed", e);
        } finally {

            CLIENT_HANDLER_POOL.shutdown();
            GAME_SESSION_POOL.shutdown();
            HANGMAN_ENGINE_POOL.shutdown();
            MATCHMAKER.shutdown();
        }
    }
}