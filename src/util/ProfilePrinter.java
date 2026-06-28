package util;

import dao.PlayerStatsDAO;
import model.PlayerStats;

import java.io.PrintWriter;

public class ProfilePrinter {

    private final PlayerStatsDAO playerStatsDAO;

    public ProfilePrinter(PlayerStatsDAO playerStatsDAO) {
        this.playerStatsDAO = playerStatsDAO;
    }


    public void printPlayerProfile(String username, PrintWriter out) {
        PlayerStats stats = playerStatsDAO.getPlayerStats(username);

        if (stats == null) {
            out.println("Profile not found for: " + username);
            return;
        }

        String lastPlayed = stats.lastPlayed() != null
                ? stats.lastPlayed().toString().replace("T", " ").substring(0, 16)
                : "Never";

        out.println("╔══════════════════════════════════════════╗");
        out.println("║             PLAYER PROFILE               ║");
        out.println("╠══════════════════════════════════════════╣");
        out.printf( "║  %-14s : %-21s ║%n", "Username",    truncate(stats.username(), 21));
        out.println("╠══════════════════════════════════════════╣");
        out.printf( "║  %-14s : %-21d ║%n", "Games Played", stats.playedCount());
        out.printf( "║  %-14s : %-21d ║%n", "Total XP",     stats.totalScore());
        out.printf( "║  %-14s : %-21d ║%n", "Best Score",   stats.highestScore());
        out.printf( "║  %-14s : %-21d ║%n", "Total Wins",   stats.totalWins());
        out.printf( "║  %-14s : %-20.1f%% ║%n", "Win Rate",  stats.winPercentage());
        out.printf( "║  %-14s : %-21s ║%n", "Last Played",  lastPlayed);
        out.println("╚══════════════════════════════════════════╝");
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}