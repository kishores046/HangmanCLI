package service;

import model.PlayerResult;
import model.Status;
import model.WaitingPlayer;
import service.connection.ClientConnection;
import util.LeaderboardPrinter;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SingleModeSession  implements Session {

    private final WaitingPlayer waitingPlayer;
    private final HangmanGameEngine hangmanGameEngine;
    private final LeaderboardPrinter leaderboardPrinter;
    private final MatchHistoryService matchHistoryService;
    private final ClientConnection clientConnection;
    private final Logger logger = Logger.getLogger("SingleModeSession");

    public SingleModeSession(WaitingPlayer waitingPlayer,
                             HangmanGameEngine hangmanGameEngine,
                             LeaderboardPrinter leaderboardPrinter,
                             MatchHistoryService matchHistoryService,
                             ClientConnection clientConnection) {
        this.waitingPlayer = waitingPlayer;
        this.hangmanGameEngine = hangmanGameEngine;
        this.leaderboardPrinter = leaderboardPrinter;
        this.matchHistoryService = matchHistoryService;
        this.clientConnection=clientConnection;

    }

    @Override
    public void run() {
        try {
            ClientDisconnectHandler disconnectHandler =
                    new ClientDisconnectHandler(clientConnection, null);
            PlayerResult result =
                    hangmanGameEngine.run(waitingPlayer, clientConnection , null, disconnectHandler);

            if (disconnectHandler.isDisconnected()) return;
            if (result.status() == Status.NOTHING) {
                clientConnection.sendMessage("A server error occurred.");
                clientConnection.sendMessage("Ended");
                return;
            }

            clientConnection.sendMessage("Match Over");
            clientConnection.sendMessage("Your score: " + result.score());
            matchHistoryService.saveSinglePlayerSession(
                    waitingPlayer.getId(), result, result.status() == Status.WIN);
            leaderboardPrinter.print(clientConnection);
            clientConnection.sendMessage("Ended");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "SingleModeSession error", e);
        }
    }
}
