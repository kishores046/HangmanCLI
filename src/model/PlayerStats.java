package model;

import java.io.Serializable;
import java.time.LocalDateTime;

public record PlayerStats(String username, int playedCount, int highestScore, int totalScore, LocalDateTime lastPlayed,
                          int totalWins,double winPercentage) implements Serializable {
}


