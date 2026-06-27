package service;

import dao.PlayerStatsDAO;
import dao.WordsStatsDAO;
import model.PlayerResult;
import model.Status;
import model.WaitingPlayer;
import util.HikariConnectionManager;
import util.PasswordUtil;

import javax.sql.DataSource;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HangmanGameEngine {


    private final PlayerStatsDAO dao;
    private final WordsStatsDAO wso;
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

    public HangmanGameEngine(DataSource dataSource) {
        this.dao=new PlayerStatsDAO(dataSource);
        this.wso=new WordsStatsDAO(dataSource);
    }

    public PlayerResult run(WaitingPlayer waitingPlayer,BufferedReader in,PrintWriter out,ChatService chatService,ClientDisconnectHandler clientDisconnectHandler) {

        int score = 0;
        String username = waitingPlayer.getUsername();
        int hintsUsed=0;
        int hintPenalty=0;
        int wrongAttempts = 0;
        long end=0;
        long start=0;
        try {
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
                return new PlayerResult(username, 0,Status.NOTHING,0,0);
            }
            chosenWord = chosenWord.toLowerCase().trim();


            char[] display = new char[chosenWord.length()];
            Arrays.fill(display, '_');
            Set<Character> guessedLetters = new HashSet<>();


            out.println(HANGMAN_FRAMES[0]);
            out.println("Word: " + new String(display));
            start = System.nanoTime();
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

            end = System.nanoTime();
            if (new String(display).equals(chosenWord)) {
                score = calculateScore(wrongAttempts, start, end, hintPenalty);
                out.println("Congratulations...");
                dao.updatePlayerStats(username, score, 1);
                return new PlayerResult(username, score, Status.WIN ,wrongAttempts, (int)((end - start) / 1_000_000_000L));
            } else {
                out.println("Sorry...");
                dao.updatePlayerStats(username, 0, 0);
            }

        } catch (SocketTimeoutException | SocketException e) {
            logger.log(Level.WARNING, "Client timed out or disconnected: {0}", username);
            clientDisconnectHandler.handleClientDisconnect(out);
            return new PlayerResult(username, 0, Status.NOTHING, wrongAttempts,
                    (int)((end - start) / 1_000_000_000L));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error during game for user: " + username, e);
            return new PlayerResult(username, 0, Status.NOTHING, wrongAttempts,
                    (int)((end - start) / 1_000_000_000L));
        }

        return new PlayerResult(username, score, Status.LOSE, wrongAttempts, (int)((end - start) / 1_000_000_000L));
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