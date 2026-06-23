package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import util.DBConnection;

public class WordsStatsDAO {
    public String getWordUnderGivenCategory(int choice){
        String sql = "SELECT word FROM words WHERE category=? ORDER BY rand() LIMIT 1";
        String chosenWord=null;
        try (Connection conn=DBConnection.getConnection();
             PreparedStatement psmt=conn.prepareStatement(sql)) {
            String category=getGategoryFromChoice(choice);
             psmt.setString(1,category);
             try (ResultSet rs=psmt.executeQuery();) {
                if(rs.next()){
                    chosenWord=rs.getString("word");
                }
             } 
        } catch (SQLException e) {
            System.out.println("Error occured"+e.getMessage());
        }
        return chosenWord;
    }
    public String getGategoryFromChoice(int choice){
        return switch(choice){
            case 1 -> "Comic-Series";
            case 2 -> "Thriller-Movies";
            case 3 ->  "SciFi-Movies";
            default -> "Comic-Series";
        };
    }
}
