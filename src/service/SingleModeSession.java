package service;

import model.PlayerResult;
import model.Status;
import model.WaitingPlayer;
import util.LeaderboardPrinter;

import java.io.*;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SingleModeSession  implements Session {

    private final WaitingPlayer waitingPlayer;
    private final HangmanGameEngine hangmanGameEngine;
    private final LeaderboardPrinter leaderboardPrinter;
    private final MatchHistoryService matchHistoryService;
    private final BufferedReader in;
    private final PrintWriter out;
    private final Logger logger = Logger.getLogger("SingleModeSession");

    public SingleModeSession(WaitingPlayer waitingPlayer,
                             HangmanGameEngine hangmanGameEngine,
                             LeaderboardPrinter leaderboardPrinter,
                             MatchHistoryService matchHistoryService,
                             BufferedReader in,
                             PrintWriter out) {
        this.waitingPlayer = waitingPlayer;
        this.hangmanGameEngine = hangmanGameEngine;
        this.leaderboardPrinter = leaderboardPrinter;
        this.matchHistoryService = matchHistoryService;
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {
        try {
            ClientDisconnectHandler disconnectHandler =
                    new ClientDisconnectHandler(out, null);
            PlayerResult result =
                    hangmanGameEngine.run(waitingPlayer, in, out, null, disconnectHandler);

            if (disconnectHandler.isDisconnected()) return;
            if (result.status() == Status.NOTHING) {
                out.println("A server error occurred.");
                out.println("Ended");
                return;
            }

            out.println("Match Over");
            out.println("Your score: " + result.score());
            matchHistoryService.saveSinglePlayerSession(
                    waitingPlayer.getId(), result, result.status() == Status.WIN);
            leaderboardPrinter.print(out);
            out.println("Ended");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "SingleModeSession error", e);
        }
    }
}
