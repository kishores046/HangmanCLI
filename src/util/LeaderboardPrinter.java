package util;

import dao.PlayerStatsDAO;
import model.PlayerStats;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.util.List;

public class LeaderboardPrinter {

    private final PlayerStatsDAO dao;
    private static final int TOP_N = 5;

    public LeaderboardPrinter(DataSource dataSource) {
        this.dao = new PlayerStatsDAO(dataSource);
    }


    public void print(PrintWriter out) {
        List<PlayerStats> top = dao.getTopNPlayers(TOP_N);

        out.println("╔═══╦══════════════╦══════════╦═══════╦══════╦════════╦═════════╗");
        out.println("║              TOP " + TOP_N + " LEADERBOARD                    ║");
        out.println("╠═══╬══════════════╬══════════╬═══════╬══════╬════════╬═════════╣");
        out.println("║ # ║ Username     ║ Total XP ║ Best  ║ Wins ║ Played ║ Win%    ║");
        out.println("╠═══╬══════════════╬══════════╬═══════╬══════╬════════╬═════════╣");

        if (top.isEmpty()) {
            out.println("║                  No scores recorded yet.                    ║");
        } else {
            for (int i = 0; i < top.size(); i++) {
                PlayerStats p = top.get(i);
               out.printf("║ %-1d ║ %-12s ║ %-8d ║ %-5d ║ %-4d ║ %-6d ║ %-6.1f%% ║%n",
                        i + 1,
                        truncate(p.username(), 12),
                        p.totalScore(),
                        p.highestScore(),
                        p.totalWins(),
                        p.playedCount(),
                        p.winPercentage());
            }
        }
        out.println("╚═══╩══════════════╩══════════╩═══════╩══════╩════════╩═════════╝");
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}