package dao;

import model.SinglePlayerSession;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SingleModeSessionDAO {

    private final DataSource dataSource;
    private static final Logger logger=Logger.getLogger("Single Mode Session DAO");

    public SingleModeSessionDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    public void save(int playerId, int score, int noOfAttemptsTaken,
                     int secondsTaken, boolean won) {

        String sql =
                "INSERT INTO single_player_sessions " +
                        "(player_id, score, wrong_attempts, duration_seconds, won) " +
                        "VALUES (?, ?, ?, ?, ?)";


        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, playerId);
            stmt.setInt(2, score);
            stmt.setInt(3, noOfAttemptsTaken);
            stmt.setInt(4, secondsTaken);
            stmt.setBoolean(5, won);
            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to save single player session", e);
        }
    }

    public List<SinglePlayerSession> getHistory(int playerId, int limit) {
        String sql =
                "SELECT score, wrong_attempts, duration_seconds, won, played_at " +
                        "FROM single_player_sessions " +
                        "WHERE player_id = ? " +
                        "ORDER BY played_at DESC " +
                        "LIMIT ?";

        List<SinglePlayerSession> results = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, playerId);
            stmt.setInt(2, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(new SinglePlayerSession(
                            rs.getInt("score"),
                            rs.getInt("wrong_attempts"),
                            rs.getInt("duration_seconds"),
                            rs.getBoolean("won"),
                            rs.getObject("played_at", LocalDateTime.class)
                    ));
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to fetch single player history for: " + playerId, e);
        }

        return results;
    }
}
