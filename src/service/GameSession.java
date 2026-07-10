package service;

import model.PlayerResult;
import model.WaitingPlayer;
import service.connection.ClientConnection;
import util.LeaderboardPrinter;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameSession implements Session {

    private final WaitingPlayer waitingPlayer1;
    private final WaitingPlayer waitingPlayer2;
    private final HangmanGameEngine hangmanGameEngine;
    private final ExecutorService hangmanEngineExecutor;
    private final MatchHistoryService matchHistoryService;
    private static final Logger logger = Logger.getLogger("GameSession");
    private final LeaderboardPrinter leaderboardPrinter;

    public GameSession(WaitingPlayer waitingPlayer1,
                       WaitingPlayer waitingPlayer2,
                       ExecutorService hangmanEngineExecutor,
                       HangmanGameEngine hangmanGameEngine,
                       LeaderboardPrinter leaderboardPrinter,
                       MatchHistoryService matchHistoryService) {

        this.waitingPlayer1 = waitingPlayer1;
        this.waitingPlayer2 = waitingPlayer2;
        this.hangmanEngineExecutor = hangmanEngineExecutor;
        this.hangmanGameEngine = hangmanGameEngine;
        this.leaderboardPrinter = leaderboardPrinter;
        this.matchHistoryService = matchHistoryService;
    }

    @Override
    public void run() {

        ClientConnection player1 = waitingPlayer1.getClientConnection();
        ClientConnection player2 = waitingPlayer2.getClientConnection();

        try {

            player1.sendMessage("MATCH_FOUND");
            player1.sendMessage("Opponent found! Starting game...");

            player2.sendMessage("MATCH_FOUND");
            player2.sendMessage("Opponent found! Starting game...");

            ChatService chatService =
                    new ChatService(player1, player2);

            ClientDisconnectHandler disconnectHandler =
                    new ClientDisconnectHandler(player1, player2);

            CompletableFuture<PlayerResult> future1 =
                    CompletableFuture.supplyAsync(
                            () -> hangmanGameEngine.run(
                                    waitingPlayer1,
                                    player1,
                                    chatService,
                                    disconnectHandler),
                            hangmanEngineExecutor);

            CompletableFuture<PlayerResult> future2 =
                    CompletableFuture.supplyAsync(
                            () -> hangmanGameEngine.run(
                                    waitingPlayer2,
                                    player2,
                                    chatService,
                                    disconnectHandler),
                            hangmanEngineExecutor);

            PlayerResult result1 = future1.join();
            PlayerResult result2 = future2.join();

            if (disconnectHandler.isDisconnected()) {
                return;
            }

            if (result1.score() > result2.score()) {

                announceResult(
                        player1, result1, "YOU WIN!",
                        player2, result2, "YOU LOSE!");

                matchHistoryService.saveMatch(
                        waitingPlayer1.getId(),
                        waitingPlayer2.getId(),
                        waitingPlayer1.getId(),
                        result1.score(),
                        result2.score(),
                        result1.secondsTaken(),
                        result2.secondsTaken(),
                        "player1_win");

            } else if (result1.score() < result2.score()) {

                announceResult(
                        player2, result2, "YOU WIN!",
                        player1, result1, "YOU LOSE!");

                matchHistoryService.saveMatch(
                        waitingPlayer1.getId(),
                        waitingPlayer2.getId(),
                        waitingPlayer2.getId(),
                        result1.score(),
                        result2.score(),
                        result1.secondsTaken(),
                        result2.secondsTaken(),
                        "player2_win");

            } else {

                announceResult(
                        player1, result1, "MATCH DRAWN!",
                        player2, result2, "MATCH DRAWN!");

                matchHistoryService.saveMatch(
                        waitingPlayer1.getId(),
                        waitingPlayer2.getId(),
                        null,
                        result1.score(),
                        result2.score(),
                        result1.secondsTaken(),
                        result2.secondsTaken(),
                        "draw");
            }

            leaderboardPrinter.print(player1);
            player1.sendMessage("Ended");

            leaderboardPrinter.print(player2);
            player2.sendMessage("Ended");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to execute game session", e);
        }
    }

    private void announceResult(
            ClientConnection winner,
            PlayerResult winnerResult,
            String winnerMsg,
            ClientConnection loser,
            PlayerResult loserResult,
            String loserMsg) {

        winner.sendMessage("MATCH OVER");
        winner.sendMessage("Your Score: " + winnerResult.score());
        winner.sendMessage("Opponent Score: " + loserResult.score());
        winner.sendMessage(winnerMsg);

        loser.sendMessage("MATCH OVER");
        loser.sendMessage("Your Score: " + loserResult.score());
        loser.sendMessage("Opponent Score: " + winnerResult.score());
        loser.sendMessage(loserMsg);
    }
}