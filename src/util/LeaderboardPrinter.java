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

    /**
     * Prints the top-N leaderboard table over the given writer.
     *
     * Columns: Rank · Username · Total XP · Best · Wins · Played
     *
     * Border math (each cell: 1 space pad left + content + 1 space pad right):
     *   ║ # ║ Username     ║ Total XP ║ Best  ║ Wins ║ Played ║
     *    1+1  1+12+1         1+8+1     1+5+1   1+4+1  1+6+1
     */
    public void print(PrintWriter out) {
        List<PlayerStats> top = dao.getTopNPlayers(TOP_N);

        out.println("╔═══╦══════════════╦══════════╦═══════╦══════╦════════╗");
        out.println("║   ║              ║  TOP " + TOP_N + "   ║  LEADERBOARD  ║");
        out.println("╠═══╬══════════════╬══════════╬═══════╬══════╬════════╣");
        out.println("║ # ║ Username     ║ Total XP ║ Best  ║ Wins ║ Played ║");
        out.println("╠═══╬══════════════╬══════════╬═══════╬══════╬════════╣");

        if (top.isEmpty()) {
            out.println("║           No scores recorded yet.                  ║");
        } else {
            for (int i = 0; i < top.size(); i++) {
                PlayerStats p = top.get(i);
                out.printf("║ %-1d ║ %-12s ║ %-8d ║ %-5d ║ %-4d ║ %-6d ║%n",
                        i + 1,
                        truncate(p.getUsername(), 12),
                        p.getTotalScore(),
                        p.getHighestScore(),
                        p.getTotalWins(),       // ← new column
                        p.getPlayedCount());
            }
        }
        out.println("╚═══╩══════════════╩══════════╩═══════╩══════╩════════╝");
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}