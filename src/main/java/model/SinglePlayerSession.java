package model;

import java.time.LocalDateTime;

public record SinglePlayerSession(
        int score,
        int wrongAttempts,
        int durationSeconds,
        boolean won,
        LocalDateTime playedAt
) {}