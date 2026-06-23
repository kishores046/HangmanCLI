package dao;

import model.PlayerStats;
import util.DBConnection;
import util.PasswordUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerStatsDAO {

    private static final Logger logger = Logger.getLogger("PlayerStatsDAO");

    // ── Authentication ────────────────────────────────────────────────────────

    /**
     * Returns true if the username already exists in the table.
     * Used before registration to decide the auth path.
     */
    public boolean usernameExists(String username) {
        String sql = "SELECT 1 FROM player_stats WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to check username existence: " + username, e);
            return false;
        }
    }

    /**
     * Registers a brand-new player with a hashed password.
     * Returns false if the username was already taken (race condition guard).
     */
    public boolean registerPlayer(String username, String passwordHash) {
        String sql =
                "INSERT INTO player_stats " +
                        "(username, password_hash, played_count, highest_score, total_score, last_played) " +
                        "VALUES (?, ?, 0, 0, 0, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();
            return true;
        } catch (SQLIntegrityConstraintViolationException e) {
            logger.log(Level.WARNING, "Username already taken (race): " + username);
            return false;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to register player: " + username, e);
            return false;
        }
    }

    /**
     * Verifies a login attempt. Returns true if credentials match.
     */
    public boolean authenticate(String username, String plaintextPassword) {
        String sql = "SELECT password_hash FROM player_stats WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return PasswordUtil.verify(plaintextPassword, rs.getString("password_hash"));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to authenticate: " + username, e);
        }
        return false;
    }

    // ── Stats ─────────────────────────────────────────────────────────────────

    public PlayerStats getPlayerStats(String username) {
        String sql =
                "SELECT username, played_count, highest_score, total_score, last_played " +
                        "FROM player_stats WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new PlayerStats(
                            rs.getString("username"),
                            rs.getInt("played_count"),
                            rs.getInt("highest_score"),
                            rs.getInt("total_score"),
                            rs.getObject("last_played", LocalDateTime.class)
                    );
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to fetch stats for: " + username, e);
        }
        return null;
    }

    /**
     * Pure UPDATE — player is guaranteed to exist after auth/registration.
     * No UPSERT needed anymore; that complexity is gone.
     */
    public void updatePlayerStats(String username, int score) {
        String sql =
                "UPDATE player_stats SET " +
                        "  played_count  = played_count + 1, " +
                        "  highest_score = GREATEST(highest_score, ?), " +
                        "  total_score   = total_score + ?, " +
                        "  last_played   = ? " +
                        "WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, score);
            stmt.setInt(2, score);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(4, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to update stats for: " + username, e);
        }
    }

    public List<PlayerStats> getTopNPlayers(int n) {
        String sql =
                "SELECT username, played_count, highest_score, total_score, last_played " +
                        "FROM player_stats " +
                        "ORDER BY total_score DESC, highest_score DESC " +
                        "LIMIT ?";
        List<PlayerStats> result = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, n);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(new PlayerStats(
                            rs.getString("username"),
                            rs.getInt("played_count"),
                            rs.getInt("highest_score"),
                            rs.getInt("total_score"),
                            rs.getObject("last_played", LocalDateTime.class)
                    ));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching leaderboard", e);
        }
        return result;
    }
}