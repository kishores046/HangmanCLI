
package net;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameServer {
    private static final ExecutorService pool=Executors.newFixedThreadPool(10);
    private static final Logger serverLogger=Logger.getLogger("Server logger");
    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket()) {
             SocketAddress socketAddress=new InetSocketAddress("10.118.130.187", 8080);
             serverSocket.bind(socketAddress);
             
            serverLogger.log(Level.INFO,"Hangman Server is running...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                serverLogger.log(Level.INFO, "New client entered"+clientSocket);
                pool.execute(new ClientHandler(clientSocket));
            }
        } catch (Exception e) {
           serverLogger.log(Level.SEVERE,e.getMessage());
        }finally{
            pool.shutdown();
        }
    }
}

