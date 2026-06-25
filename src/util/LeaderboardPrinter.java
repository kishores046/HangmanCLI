package util;

import dao.PlayerStatsDAO;
import model.PlayerStats;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.util.List;

public class LeaderboardPrinter {


    private static final DataSource DATA_SOURCE=HikariConnectionManager.getDataSource();
    private static final PlayerStatsDAO dao = new PlayerStatsDAO(DATA_SOURCE);
    private static final int TOP_N = 5;
    public static void print(PrintWriter out) {
        List<PlayerStats> top = dao.getTopNPlayers(TOP_N);
        out.println("╔═══════════════════════════════════════════════════╗");
        out.println("║                  TOP 5 LEADERBOARD                ║");
        out.println("╠═══╦══════════════╦═════════╦═══════╦══════════════╣");
        out.println("║ # ║ Username     ║ Total   ║ Best  ║ Played       ║");
        out.println("╠═══╬══════════════╬═════════╬═══════╬══════════════╣");

        if (top.isEmpty()) {
            out.println("║            No scores recorded yet.             ║");
        } else {
            for (int i = 0; i < top.size(); i++) {
                PlayerStats p = top.get(i);
                out.printf("║ %-1d ║ %-12s ║ %-7d ║ %-5d ║ %-12d ║%n",
                        i + 1,
                        truncate(p.getUsername(), 12),
                        p.getTotalScore(),
                        p.getHighestScore(),
                        p.getPlayedCount());
            }
        }
        out.println("╚═══╩══════════════╩═════════╩═══════╩══════════════╝");
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}