package service;

import model.WaitingPlayer;
import util.LeaderboardPrinter;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MatchMakingService {

    private final BlockingQueue<WaitingPlayer> waitingQueue = new LinkedBlockingQueue<>();
    private final ExecutorService gameSessionExecutor;
    private final ExecutorService hangmanEngineExecutor;
    private final HangmanGameEngine hangmanGameEngine;
    private final LeaderboardPrinter leaderboardPrinter;

    private final ExecutorService matchMakerThread = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "matchmaker-thread");
        t.setDaemon(true);
        return t;
    });

    private static final Logger logger = Logger.getLogger("MatchMakingService");

    public MatchMakingService(ExecutorService gameSessionExecutor,
                              ExecutorService hangmanEngineExecutor,
                              HangmanGameEngine hangmanGameEngine, LeaderboardPrinter leaderboardPrinter) {
        this.gameSessionExecutor = gameSessionExecutor;
        this.hangmanEngineExecutor = hangmanEngineExecutor;
        this.hangmanGameEngine=hangmanGameEngine;
        this.leaderboardPrinter = leaderboardPrinter;
        matchMakerThread.submit(this::matchMakeWaitingPlayers);
    }

    public void enqueue(WaitingPlayer player) {
        waitingQueue.add(player);
        logger.log(Level.INFO, "Player queued from {0} (queue size: {1})",
                new Object[]{player.getSocket().getInetAddress().getHostAddress(), waitingQueue.size()});
       try{
            PrintWriter out=new PrintWriter(new OutputStreamWriter(player.getSocket().getOutputStream()),true);
            out.println("WAITING");
            out.println("Waiting for another player...");
        }catch (IOException e){
            logger.log(Level.SEVERE,e.getMessage());
        }

    }

    private void matchMakeWaitingPlayers() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                WaitingPlayer p1 = waitingQueue.take();
                WaitingPlayer p2 = waitingQueue.take();
                logger.log(Level.INFO, "Matched {0} vs {1}",
                        new Object[]{p1.getUsername(), p2.getUsername()});
                gameSessionExecutor.submit(new GameSession(p1, p2, hangmanEngineExecutor,hangmanGameEngine,leaderboardPrinter));
            }
        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
            logger.log(Level.INFO, "Matchmaker thread interrupted — shutting down");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Matchmaker encountered an error", e);
        }
    }

    /**
     * Call from GameServer.main() during orderly shutdown.
     * Interrupts the matchmaking loop; any players still in the queue are dropped.
     */
    public void shutdown() {
        matchMakerThread.shutdownNow();
    }
}