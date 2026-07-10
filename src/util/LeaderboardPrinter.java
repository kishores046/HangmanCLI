package util;

import dao.PlayerStatsDAO;
import model.PlayerStats;
import service.connection.ClientConnection;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.util.List;

public class LeaderboardPrinter {

    private final PlayerStatsDAO dao;
    private static final int TOP_N = 5;


    public LeaderboardPrinter(DataSource dataSource) {
        this.dao = new PlayerStatsDAO(dataSource);
    }


    public void print(ClientConnection clientConnection) {
        List<PlayerStats> top = dao.getTopNPlayers(TOP_N);

        clientConnection.sendMessage("╔═══╦══════════════╦══════════╦═══════╦══════╦════════╦═════════╗");
         clientConnection.sendMessage("║              TOP " + TOP_N + " LEADERBOARD                    ║");
         clientConnection.sendMessage("╠═══╬══════════════╬══════════╬═══════╬══════╬════════╬═════════╣");
         clientConnection.sendMessage("║ # ║ Username     ║ Total XP ║ Best  ║ Wins ║ Played ║ Win%    ║");
         clientConnection.sendMessage("╠═══╬══════════════╬══════════╬═══════╬══════╬════════╬═════════╣");

        if (top.isEmpty()) {
             clientConnection.sendMessage("║                  No scores recorded yet.                    ║");
        } else {
            for (int i = 0; i < top.size(); i++) {
                PlayerStats p = top.get(i);
               clientConnection.sendFormatted("║ %-1d ║ %-12s ║ %-8d ║ %-5d ║ %-4d ║ %-6d ║ %-6.1f%% ║%n",
                        i + 1,
                        truncate(p.username(), 12),
                        p.totalScore(),
                        p.highestScore(),
                        p.totalWins(),
                        p.playedCount(),
                        p.winPercentage());
            }
        }
         clientConnection.sendMessage("╚═══╩══════════════╩══════════╩═══════╩══════╩════════╩═════════╝");
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}