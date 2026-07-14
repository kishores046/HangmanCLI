package util;

import dao.PlayerStatsDAO;
import model.PlayerStats;
import service.connection.ClientConnection;

public class ProfilePrinter {

    private final PlayerStatsDAO playerStatsDAO;

    public ProfilePrinter(PlayerStatsDAO playerStatsDAO) {
        this.playerStatsDAO = playerStatsDAO;
    }


    public void printPlayerProfile(String username, ClientConnection clientConnection) {
        PlayerStats stats = playerStatsDAO.getPlayerStats(username);

        if (stats == null) {
            clientConnection.sendMessage("Profile not found for: " + username);
            return;
        }

        String lastPlayed = stats.lastPlayed() != null
                ? stats.lastPlayed().toString().replace("T", " ").substring(0, 16)
                : "Never";

        clientConnection.sendMessage("╔══════════════════════════════════════════╗");
        clientConnection.sendMessage("║             PLAYER PROFILE               ║");
        clientConnection.sendMessage("╠══════════════════════════════════════════╣");
        clientConnection.sendFormatted( "║  %-14s : %-21s ║%n", "Username",    truncate(stats.username(), 21));
        clientConnection.sendMessage("╠══════════════════════════════════════════╣");
        clientConnection.sendFormatted( "║  %-14s : %-21d ║%n", "Games Played", stats.playedCount());
        clientConnection.sendFormatted( "║  %-14s : %-21d ║%n", "Total XP",     stats.totalScore());
        clientConnection.sendFormatted( "║  %-14s : %-21d ║%n", "Best Score",   stats.highestScore());
        clientConnection.sendFormatted( "║  %-14s : %-21d ║%n", "Total Wins",   stats.totalWins());
        clientConnection.sendFormatted( "║  %-14s : %-20.1f%% ║%n", "Win Rate",  stats.winPercentage());
        clientConnection.sendFormatted( "║  %-14s : %-21s ║%n", "Last Played",  lastPlayed);
        clientConnection.sendMessage("╚══════════════════════════════════════════╝");
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}