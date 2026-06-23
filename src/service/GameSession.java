package service;

import model.PlayerResult;
import model.WaitingPlayer;
import util.LeaderboardPrinter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameSession implements Session {

    private final WaitingPlayer waitingPlayer1;
    private final WaitingPlayer waitingPlayer2;
    private final HangmanGameEngine hangmanGameEngine = new HangmanGameEngine();


    private final ExecutorService hangmanEngineExecutor;

    private static final Logger logger = Logger.getLogger("GameSession");

    public GameSession(WaitingPlayer waitingPlayer1,
                       WaitingPlayer waitingPlayer2,
                       ExecutorService hangmanEngineExecutor) {
        this.waitingPlayer1 = waitingPlayer1;
        this.waitingPlayer2 = waitingPlayer2;
        this.hangmanEngineExecutor = hangmanEngineExecutor;
    }

    @Override
    public void run() {




        try (PrintWriter out1 = new PrintWriter(waitingPlayer1.getSocket().getOutputStream(), true);
             PrintWriter out2 = new PrintWriter(waitingPlayer2.getSocket().getOutputStream(), true)) {

            out1.println("MATCH_FOUND");
            out1.println("Opponent found! Starting game...");

            out2.println("MATCH_FOUND");
            out2.println("Opponent found! Starting game...");
            CompletableFuture<PlayerResult> future1 =
                    CompletableFuture.supplyAsync(
                            () -> hangmanGameEngine.run(waitingPlayer1), hangmanEngineExecutor);

            CompletableFuture<PlayerResult> future2 =
                    CompletableFuture.supplyAsync(
                            () -> hangmanGameEngine.run(waitingPlayer2), hangmanEngineExecutor);

            PlayerResult result1 = future1.join();
            PlayerResult result2 = future2.join();

            if (result1.getScore() > result2.getScore()) {
                announceResult(out1, result1, "YOU WIN!",    out2, result2, "YOU LOSE!");
            } else if (result1.getScore() < result2.getScore()) {
                announceResult(out2, result2, "YOU WIN!",    out1, result1, "YOU LOSE!");
            } else {
                announceResult(out1, result1, "MATCH DRAWN!", out2, result2, "MATCH DRAWN!");
            }


            LeaderboardPrinter.print(out1);
            out1.println("Ended");
            LeaderboardPrinter.print(out2);
            out2.println("Ended");

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to send match result", e);
        }
    }

    /**
     * Sends the full result block to both players.
     *
     * @param winnerOut    writer for the player who won (or drew)
     * @param winnerResult their result
     * @param winnerMsg    e.g. "YOU WIN!" or "MATCH DRAWN!"
     * @param loserOut     writer for the other player
     * @param loserResult  their result
     * @param loserMsg     e.g. "YOU LOSE!" or "MATCH DRAWN!"
     */
    private void announceResult(PrintWriter winnerOut, PlayerResult winnerResult, String winnerMsg,
                                PrintWriter loserOut,  PlayerResult loserResult,  String loserMsg) {
        winnerOut.println("MATCH OVER");
        winnerOut.println("Your Score: "     + winnerResult.getScore());
        winnerOut.println("Opponent Score: " + loserResult.getScore());
        winnerOut.println(winnerMsg);

        loserOut.println("MATCH OVER");
        loserOut.println("Your Score: "     + loserResult.getScore());
        loserOut.println("Opponent Score: " + winnerResult.getScore());
        loserOut.println(loserMsg);
    }
}