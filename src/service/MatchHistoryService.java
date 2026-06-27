package service;

import dao.MatchHistoryDAO;
import dao.SingleModeSessionDAO;
import dao.SingleModeSessionDAO;
import model.MatchHistory;
import model.PlayerResult;
import model.SinglePlayerSession;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;

public class MatchHistoryService {


    private final MatchHistoryDAO matchHistoryDAO;
    private static final int DEFAULT_LIMIT = 10;
    private final SingleModeSessionDAO singleModeSessionDAO;

    public MatchHistoryService(MatchHistoryDAO matchHistoryDAO, SingleModeSessionDAO singeModeSessionDao) {
        this.matchHistoryDAO = matchHistoryDAO;
        this.singleModeSessionDAO =singeModeSessionDao;
    }

    /**
     * Fetches and prints the match history for a player.
     * DAO returns the data; this class owns the formatting.
     */
    public void printMatchHistory(int playerId, String username, PrintWriter out) {
        List<MatchHistory> history = matchHistoryDAO.getMatchHistory(playerId, DEFAULT_LIMIT);
        out.println("══════════════════════════════════════════════════════");
        out.println("  Recent Matches — " + username);
        out.println("══════════════════════════════════════════════════════");

        if (history.isEmpty()) {
            out.println("  No matches played yet.");
        } else {
            for (MatchHistory m : history) {
                String outcome = resolveOutcome(m, username);
                out.printf("  %-4s  %s (%d pts, %ds) vs %s (%d pts, %ds)%n",
                        outcome,
                        m.player1Name(), m.player1Score(), m.player1DurationSeconds(),
                        m.player2Name(), m.player2Score(), m.player2DurationSeconds());
                out.printf("        %s%n",
                        m.playedAt().toString().replace("T", " ").substring(0, 16));
            }
        }
        out.println("══════════════════════════════════════════════════════");
    }

    /**
     * Resolves the outcome label from the requesting player's perspective.
     * "draw" result → DRAW regardless of names.
     * winner_name == requesting username → WIN, otherwise LOSS.
     */
    private String resolveOutcome(MatchHistory m, String username) {
        if ("draw".equals(m.result())) return "DRAW";
        return username.equals(m.winnerName()) ? "WIN " : "LOSS";
    }

    /**
     * Saves a completed match.
     * GameSession calls this — it has all the data, no extra queries.
     */
    public void saveMatch(int player1Id, int player2Id, Integer winnerId,
                          int player1Score, int player2Score,
                          int player1Seconds, int player2Seconds,
                          String result) {
        matchHistoryDAO.saveMatch(player1Id, player2Id, winnerId,
                player1Score, player2Score,
                player1Seconds, player2Seconds,
                result);
    }

    public void saveSinglePlayerSession(int playerId, PlayerResult result, boolean won) {
        singleModeSessionDAO.save(playerId, result.score(),
                result.noOfAttemptsTaken(), result.secondsTaken(), won);
    }


    public void printSinglePlayerHistory(int playerId, String username, PrintWriter out) {
        List<SinglePlayerSession> history = singleModeSessionDAO.getHistory(playerId, DEFAULT_LIMIT);

        out.println("══════════════════════════════════════════════════════");
        out.println("  Solo History — " + username);
        out.println("══════════════════════════════════════════════════════");
        out.println("  # │ Result │ Score │ Wrong │ Time  │ Played at");
        out.println("  ──┼────────┼───────┼───────┼───────┼────────────────");

        if (history.isEmpty()) {
            out.println("  No solo games played yet.");
        } else {
            int rank = 1;
            for (SinglePlayerSession s : history) {
                out.printf("  %-1d │ %-6s │ %-5d │ %-5d │ %-5ds │ %s%n",
                        rank++,
                        s.won() ? "WIN" : "LOSS",
                        s.score(),
                        s.wrongAttempts(),
                        s.durationSeconds(),
                        s.playedAt().toString().replace("T", " ").substring(0, 16));
            }
        }
        out.println("══════════════════════════════════════════════════════");
    }
}
