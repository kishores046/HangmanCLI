package dao;

import java.sql.*;
import java.time.LocalDateTime;
import javax.sql.DataSource;

public class WordsStatsDAO {

    private final DataSource dataSource;

    public WordsStatsDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String getWordUnderGivenCategory(int categoryId) {
        String sql = "SELECT word FROM words WHERE category_id = ? ORDER BY rand() LIMIT 1";
        String chosenWord = null;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement psmt = conn.prepareStatement(sql)) {
            psmt.setInt(1, categoryId);
            try (ResultSet rs = psmt.executeQuery()) {
                if (rs.next()) {
                    chosenWord = rs.getString("word");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error occurred" + e.getMessage());
        }
        return chosenWord;
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
            System.out.println("Error occurred" + e.getMessage());
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
            System.out.println("Error occurred" + e.getMessage());
        }
    }
}