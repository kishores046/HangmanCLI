package dao;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

public class WordsStatsDAO {

    private final DataSource dataSource;
    private static final Logger logger=Logger.getLogger("WordsStatsDAO");

    public WordsStatsDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public WordEntry getWordEntryUnderCategory(int categoryId) {
        String sql = "SELECT wordId, word, imdb_id, plot_hint FROM words WHERE category_id = ? ORDER BY rand() LIMIT 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement psmt = conn.prepareStatement(sql)) {
            psmt.setInt(1, categoryId);
            try (ResultSet rs = psmt.executeQuery()) {
                if (rs.next()) {
                    return new WordEntry(rs.getInt("wordId"), rs.getString("word"),
                            rs.getString("imdb_id"), rs.getString("plot_hint"));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to fetch word entry for categoryId: " + categoryId, e);
        }
        return null;
    }


    public WordEntry getWordEntryUnderCategoryAndDifficulty(int categoryId, String difficulty) {
        String sql = "SELECT wordId, word, imdb_id, plot_hint FROM words " +
                "WHERE category_id = ? AND difficulty = ? ORDER BY rand() LIMIT 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement psmt = conn.prepareStatement(sql)) {
            psmt.setInt(1, categoryId);
            psmt.setString(2, difficulty);
            try (ResultSet rs = psmt.executeQuery()) {
                if (rs.next()) {
                    return new WordEntry(rs.getInt("wordId"), rs.getString("word"),
                            rs.getString("imdb_id"), rs.getString("plot_hint"));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to fetch word for category " + categoryId + " / " + difficulty, e);
        }
        return null;
    }

    public void savePlotHint(int wordId, String plot) {
        String sql = "UPDATE words SET plot_hint = ?, hint_fetched_at = ? WHERE wordId = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement psmt = conn.prepareStatement(sql)) {
            psmt.setString(1, plot);
            psmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            psmt.setInt(3, wordId);
            psmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to fetch save plot");
        }
    }



}