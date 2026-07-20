CREATE TABLE IF NOT EXISTS categories (
                                          id INT NOT NULL AUTO_INCREMENT,
                                          category_name VARCHAR(50) NOT NULL,
                                          PRIMARY KEY (id),
                                          UNIQUE KEY uq_category_name (category_name)
);

CREATE TABLE IF NOT EXISTS words (
                                     wordId INT NOT NULL AUTO_INCREMENT,
                                     word VARCHAR(75) NULL,
                                     category_id INT NULL,
                                     plot_hint TEXT NULL,
                                     hint_fetched_at TIMESTAMP NULL,
                                     imdb_id VARCHAR(20) NULL,
                                     popularity_votes INT NULL,
                                     difficulty ENUM('EASY','MEDIUM','HARD') NULL,
                                     PRIMARY KEY (wordId),
                                     KEY idx_words_category (category_id),
                                     CONSTRAINT fk_words_category
                                         FOREIGN KEY (category_id) REFERENCES categories(id)
                                             ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS player_stats (
                                            id INT NOT NULL AUTO_INCREMENT,
                                            username VARCHAR(50) NULL,
                                            password_hash VARCHAR(64) NOT NULL,
                                            played_count INT NULL,
                                            highest_score INT NULL,
                                            last_played TIMESTAMP NULL,
                                            total_score INT NOT NULL DEFAULT 0,
                                            total_wins INT NOT NULL DEFAULT 0,
                                            PRIMARY KEY (id),
                                            UNIQUE KEY uq_username (username)
);

CREATE TABLE IF NOT EXISTS matches (
                                       id INT NOT NULL AUTO_INCREMENT,
                                       player1_id INT NOT NULL,
                                       player2_id INT NOT NULL,
                                       winner_id INT NULL,
                                       player1_score INT NOT NULL,
                                       player2_score INT NOT NULL,
                                       player1_duration_seconds INT NOT NULL,
                                       player2_duration_seconds INT NOT NULL,
                                       result ENUM('player1_win','player2_win','draw') NOT NULL,
                                       played_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                                       PRIMARY KEY (id),
                                       KEY idx_matches_player1 (player1_id),
                                       KEY idx_matches_player2 (player2_id),
                                       KEY idx_matches_winner (winner_id),
                                       KEY idx_matches_played_at (played_at),
                                       CONSTRAINT fk_matches_player1 FOREIGN KEY (player1_id) REFERENCES player_stats(id),
                                       CONSTRAINT fk_matches_player2 FOREIGN KEY (player2_id) REFERENCES player_stats(id),
                                       CONSTRAINT fk_matches_winner  FOREIGN KEY (winner_id)  REFERENCES player_stats(id)
);

CREATE TABLE IF NOT EXISTS single_player_sessions (
                                                      id INT NOT NULL AUTO_INCREMENT,
                                                      player_id INT NOT NULL,
                                                      score INT NOT NULL,
                                                      wrong_attempts INT NOT NULL,
                                                      duration_seconds INT NOT NULL,
                                                      won TINYINT(1) NOT NULL,
                                                      played_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                                                      PRIMARY KEY (id),
                                                      KEY idx_sps_player (player_id),
                                                      CONSTRAINT fk_sps_player FOREIGN KEY (player_id) REFERENCES player_stats(id)
);