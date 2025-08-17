package model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class PlayerStats implements Serializable {
    private String username;
    private int playedCount;
    private int highestScore;
    private LocalDateTime lastPlayed;

    public PlayerStats(String username, int playedCount, int highestScore, LocalDateTime lastPlayed) {
        this.username = username;
        this.playedCount = playedCount;
        this.highestScore = highestScore;
        this.lastPlayed = lastPlayed;
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


