package model;


import java.time.LocalDateTime;

public record MatchHistory(
        String player1Name,
        String player2Name,
        String winnerName,
        int player1Score,
        int player2Score,
        int player1DurationSeconds,
        int player2DurationSeconds,
        String result,
        LocalDateTime playedAt
) {}