
package dao;

import model.PlayerStats;
import util.DBConnection;


import java.sql.*;
import java.time.LocalDateTime;
public class PlayerStatsDAO {

    public PlayerStats getPlayerStats(String username) {
        String sql = "SELECT * FROM player_stats WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new PlayerStats(
                        rs.getString("username"),
                        rs.getInt("played_count"),
                        rs.getInt("highest_score"),
                        rs.getTimestamp("last_played").toLocalDateTime()
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updatePlayerStats(String username, int score) {
        PlayerStats existing = getPlayerStats(username);
        if (existing == null) {
            insertNewPlayer(username, score);
        } else {
            String sql = "UPDATE player_stats SET played_count = played_count + 1, " +
                    "highest_score = GREATEST(highest_score, ?), " +
                    "last_played = ? WHERE username = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, score);
                stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                stmt.setString(3, username);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void insertNewPlayer(String username, int score) {
        String sql = "INSERT INTO player_stats(username, played_count, highest_score, last_played) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setInt(2, 1);
            stmt.setInt(3, score);
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

