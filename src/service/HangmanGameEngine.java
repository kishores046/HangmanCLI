package service;

import dao.PlayerStatsDAO;
import dao.WordsStatsDAO;
import model.PlayerResult;
import model.WaitingPlayer;
import util.HikariConnectionManager;
import util.PasswordUtil;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HangmanGameEngine {

    private static final DataSource DATA_SOURCE=HikariConnectionManager.getDataSource();
    private final PlayerStatsDAO dao = new PlayerStatsDAO(DATA_SOURCE);
    private final WordsStatsDAO wso = new WordsStatsDAO(DATA_SOURCE);
    private static final AuthenticationService authenticationService = AuthenticationService.getInstance();
    private static final int MAX_ATTEMPTS = 6;
    private static final int MAX_HINTS=4;
    private static final Logger logger = Logger.getLogger("HangmanGameEngine");


    private static final String[] HANGMAN_FRAMES = {
            "   +---+\n   |   |\n       |\n       |\n       |\n       |\n  =========",
            "   +---+\n   |   |\n   O   |\n       |\n       |\n       |\n  =========",
            "   +---+\n   |   |\n   O   |\n   |   |\n       |\n       |\n  =========",
            "   +---+\n   |   |\n   O   |\n  /|   |\n       |\n       |\n  =========",
            "   +---+\n   |   |\n   O   |\n  /|\\  |\n       |\n       |\n  =========",
            "   +---+\n   |   |\n   O   |\n  /|\\  |\n  /    |\n       |\n  =========",
            "   +---+\n   |   |\n   O   |\n  /|\\  |\n  / \\  |\n       |\n  ========="
    };

    public PlayerResult run(WaitingPlayer waitingPlayer,BufferedReader in,PrintWriter out,ChatService chatService) {
        Socket socket = waitingPlayer.getSocket();
        int score = 0;
        String username = null;
        int hintsUsed=0;
        int hintPenalty=0;

        try {

            out.println("INPUT_USERNAME");
            username = in.readLine();
            if (username == null || username.isBlank()) {
                out.println("Invalid username. Connection closing.");
                return new PlayerResult("unknown", 0);
            }
            username = username.trim();


            boolean authOk =authenticationService.handleAuth(username, in, out);
            if (!authOk) return new PlayerResult(username, 0);

            waitingPlayer.setUsername(username);


            out.println("INPUT_CATEGORY");
            out.println("Welcome " + username + "! Let's play Hangman.");
            out.println("Choose your category:");
            out.println("  1. Comic-Series");
            out.println("  2. Thriller-Movies");
            out.println("  3. SciFi-Movies");
            out.println("Enter 1, 2 or 3:");

            int choice = parseChoice(in.readLine());
            String chosenWord = wso.getWordUnderGivenCategory(choice);
            if (chosenWord == null || chosenWord.isBlank()) {
                out.println("No words available for that category. Please try again later.");
                return new PlayerResult(username, 0);
            }
            chosenWord = chosenWord.toLowerCase().trim();


            char[] display = new char[chosenWord.length()];
            Arrays.fill(display, '_');
            Set<Character> guessedLetters = new HashSet<>();
            int wrongAttempts = 0;

            out.println(HANGMAN_FRAMES[0]);
            out.println("Word: " + new String(display));
            long start = System.nanoTime();
            out.println("INPUT_GUESS");

            while (wrongAttempts < MAX_ATTEMPTS && new String(display).contains("_")) {
                out.println("Guessed so far: " + guessedLetters);
                out.println("Enter your guess client!:(single character)");

                String guessByClient = in.readLine();
                if (guessByClient == null || guessByClient.isBlank()) continue;

                if (guessByClient.trim().equalsIgnoreCase("HINT")) {
                    if (hintsUsed >= MAX_HINTS) {
                        out.println("No hints remaining!");
                    } else {

                        List<Integer> unrevealed = new ArrayList<>();
                        for (int i = 0; i < chosenWord.length(); i++)
                            if (display[i] == '_') unrevealed.add(i);
                        int idx = unrevealed.get(new Random().nextInt(unrevealed.size()));
                        display[idx] = chosenWord.charAt(idx);
                        hintsUsed++;
                        hintPenalty+=5;
                        out.println("Hint used! (" + hintsUsed + "/" + MAX_HINTS + ") — -5 points penalty");
                        out.println("Word: " + new String(display));
                    }
                    continue;
                }

                if (chatService != null && guessByClient.toUpperCase().startsWith("CHAT:")) {
                    String message = guessByClient.substring(5).trim();
                    if (!message.isBlank()) {
                        chatService.route(out, username, message);
                        out.println("CHAT_SENT");
                    }
                    continue;
                }
                char guess = Character.toLowerCase(guessByClient.charAt(0));

                if (guessedLetters.contains(guess)) {
                    out.println("You already guessed '" + guess + "'. Try a different letter.");
                    out.println("Word: " + new String(display));
                    out.println("Wrong attempts: " + wrongAttempts + "/" + MAX_ATTEMPTS);
                    continue;
                }
                guessedLetters.add(guess);

                boolean found = false;
                for (int i = 0; i < chosenWord.length(); i++) {
                    if (chosenWord.charAt(i) == guess && display[i] == '_') {
                        display[i] = guess;
                        found = true;
                    }
                }
                if (!found) wrongAttempts++;

                out.println("Word: " + new String(display));
                out.println("Wrong attempts: " + wrongAttempts + "/" + MAX_ATTEMPTS);
                out.println(HANGMAN_FRAMES[wrongAttempts]);
            }

            long end = System.nanoTime();


            if (new String(display).equals(chosenWord)) {
                score = calculateScore(wrongAttempts, start, end,hintPenalty);
                out.println("Congratulations " + username + "! You guessed the word: " + chosenWord);
                out.println("Your score: " + score);
            } else {
                out.println(HANGMAN_FRAMES[MAX_ATTEMPTS]);
                out.println("Sorry " + username + "! The word was: " + chosenWord);
                out.println("Your score: 0");
            }

            dao.updatePlayerStats(username, score);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during game for user: " + username, e);
        }

        return new PlayerResult(username != null ? username : "unknown", score);
    }

    private int parseChoice(String line) {
        if (line == null) return 1;
        try {
            int choice = Integer.parseInt(line.trim());
            return (choice >= 1 && choice <= 3) ? choice : 1;
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Invalid category input ''{0}'' — defaulting to 1", line);
            return 1;
        }
    }

    public int calculateScore(int wrongAttempts, long start, long end,int hintPenality) {
        long elapsedSeconds = (end - start) / 1_000_000_000L;
        return ((MAX_ATTEMPTS - wrongAttempts) * 10) + (int) Math.max(0L, 60L - elapsedSeconds)-hintPenality;
    }
}