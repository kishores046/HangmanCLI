package dao;

import model.MatchHistory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MatchHistoryDAO {

    private final DataSource dataSource;
    private static final Logger logger=Logger.getLogger("Match History DAO");
    public MatchHistoryDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }



    /**
     * Fetches the last {@code limit} matches involving the given player.
     * All player names resolved in one JOIN — no N+1.
     * Returns an empty list on error; never null.
     */
    public List<MatchHistory> getMatchHistory(int playerId, int limit) {
        String sql =
                "SELECT " +
                        "  p1.username              AS player1_name, " +
                        "  p2.username              AS player2_name, " +
                        "  w.username               AS winner_name, " +
                        "  m.player1_score, " +
                        "  m.player2_score, " +
                        "  m.player1_duration_seconds, " +
                        "  m.player2_duration_seconds, " +
                        "  m.result, " +
                        "  m.played_at " +
                        "FROM matches m " +
                        "JOIN  player_stats p1 ON m.player1_id = p1.id " +
                        "JOIN  player_stats p2 ON m.player2_id = p2.id " +
                        "LEFT JOIN player_stats w  ON m.winner_id  = w.id " +
                        "WHERE m.player1_id = ? OR m.player2_id = ? " +
                        "ORDER BY m.played_at DESC " +
                        "LIMIT ?";

        List<MatchHistory> results = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, playerId);
            stmt.setInt(2, playerId);
            stmt.setInt(3, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(new MatchHistory(
                            rs.getString("player1_name"),
                            rs.getString("player2_name"),
                            rs.getString("winner_name"),   // null for draw
                            rs.getInt("player1_score"),
                            rs.getInt("player2_score"),
                            rs.getInt("player1_duration_seconds"),
                            rs.getInt("player2_duration_seconds"),
                            rs.getString("result"),
                            rs.getObject("played_at", java.time.LocalDateTime.class)
                    ));
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to fetch match history for playerId: " + playerId, e);
        }

        return results;
    }

    /**
     * Persists a completed match.
     * winnerId is null for draws.
     */


    public void saveMatch(int player1Id, int player2Id, Integer winnerId,
                          int player1Score, int player2Score,
                          int player1Seconds, int player2Seconds,
                          String result) {
        String sql =
                "INSERT INTO matches " +
                        "(player1_id, player2_id, winner_id, player1_score, player2_score, " +
                        " player1_duration_seconds, player2_duration_seconds, result) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, player1Id);
            stmt.setInt(2, player2Id);
            if (winnerId != null) stmt.setLong(3, winnerId);
            else stmt.setNull(3, Types.INTEGER);             // draw — null winner
            stmt.setInt(4, player1Score);
            stmt.setInt(5, player2Score);
            stmt.setInt(6, player1Seconds);
            stmt.setInt(7, player2Seconds);
            stmt.setString(8, result);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to save match history", e);
        }
    }



}
