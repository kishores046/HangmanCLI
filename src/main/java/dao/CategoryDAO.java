package dao;

import model.Category;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CategoryDAO {

    private final DataSource dataSource;
    private static final Logger logger = Logger.getLogger("CategoryDAO");

    public CategoryDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** Ordered by id so the menu numbering stays stable across calls. */
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT id, category_name FROM categories ORDER BY id";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement psmt = conn.prepareStatement(sql);
             ResultSet rs = psmt.executeQuery()) {
            while (rs.next()) {
                categories.add(new Category(rs.getInt("id"), rs.getString("category_name")));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to load categories", e);
        }
        return categories;
    }
}