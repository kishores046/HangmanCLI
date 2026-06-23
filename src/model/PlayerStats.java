package model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class PlayerStats implements Serializable {
    private final String username;
    private int playedCount;
    private int highestScore;
    private LocalDateTime lastPlayed;
    private int totalScore;

    public PlayerStats(String username, int playedCount, int highestScore,int totalScore, LocalDateTime lastPlayed) {
        this.username = username;
        this.playedCount = playedCount;
        this.highestScore = highestScore;
        this.lastPlayed = lastPlayed;
        this.totalScore=totalScore;
    }

    public int getTotalScore() {
        return totalScore;
    }
    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public String getUsername() {
        return username; 
    }
    public int getPlayedCount(){ 
        return playedCount; 
    }
    public int getHighestScore() {
        return highestScore; 
    }
    public LocalDateTime getLastPlayed() {
        return lastPlayed; 
    }
    public void setPlayedCount(int playedCount) {
         this.playedCount = playedCount;
    }
    public void setHighestScore(int highestScore) { 
        this.highestScore = highestScore; 
    }
    public void setLastPlayed(LocalDateTime lastPlayed) {
        this.lastPlayed = lastPlayed; 
    }
}


