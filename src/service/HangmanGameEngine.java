package service;

import dao.CategoryDAO;
import dao.PlayerStatsDAO;
import dao.WordsStatsDAO;
import model.Category;
import model.PlayerResult;
import model.Status;
import model.WaitingPlayer;
import javax.sql.DataSource;
import java.io.*;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import dao.WordEntry;
import util.OmdbClient;

public class HangmanGameEngine {


    private final PlayerStatsDAO dao;
    private final WordsStatsDAO wso;
    private static final AuthenticationService authenticationService = AuthenticationService.getInstance();
    private static final int MAX_HINTS=4;
    private static final int PLOT_HINT_PENALTY = 15;
    private static final int MAX_PLOT_HINTS = 1;
    private static final Logger logger = Logger.getLogger("HangmanGameEngine");
    private final CategoryDAO categoryDAO;

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
        this.categoryDAO = new CategoryDAO(dataSource);
    }

    public PlayerResult run(WaitingPlayer waitingPlayer, BufferedReader in, PrintWriter out, ChatService chatService, ClientDisconnectHandler clientDisconnectHandler) {
        int score = 0;
        String username = waitingPlayer.getUsername();
        int hintsUsed = 0;
        int hintPenalty = 0;
        int plotHintsUsed = 0;
        int wrongAttempts = 0;
        long end = 0;
        long start = 0;
        try {

            List<Category> categories = categoryDAO.getAllCategories();
            if (categories.isEmpty()) {
                out.println("No categories configured. Contact server admin.");
                return new PlayerResult(username, 0, Status.NOTHING, 0, 0);
            }

            out.println("INPUT_CATEGORY");
            out.println("Welcome " + username + "! Let's play Hangman.");
            out.println("Choose your category:");
            for (int i = 0; i < categories.size(); i++) {
                out.println("  " + (i + 1) + ". " + categories.get(i).name());
            }
            out.println("Enter 1-" + categories.size() + ":");
            out.println("CATEGORY_END");
            int choiceIndex = parseChoice(in.readLine(), categories.size()); // 0-based index
            int categoryId = categories.get(choiceIndex).id();
            out.println("INPUT_DIFFICULTY");
            out.println("Choose difficulty:");
            out.println("  1. Easy");
            out.println("  2. Medium");
            out.println("  3. Hard");
            out.println("Enter 1-3:");
            out.println("DIFFICULTY_END");
            String diffChoice = in.readLine();
            String difficulty = switch (diffChoice == null ? "" : diffChoice.trim()) {
                case "1" -> "EASY";
                case "3" -> "HARD";
                default -> "MEDIUM";
            };

            int maxAttempts = switch (difficulty) {
                case "EASY" -> 8;
                case "HARD" -> 4;
                default -> 6;
            };

            WordEntry entry = wso.getWordEntryUnderCategoryAndDifficulty(categoryId, difficulty);
            if (entry == null) {
                entry = wso.getWordEntryUnderCategory(categoryId);
                if (entry == null || entry.word() == null || entry.word().isBlank()) {
                    out.println("No words available for that category. Please try again later.");
                    return new PlayerResult(username, 0, Status.NOTHING, 0, 0);
                }
            }

            String chosenWord = entry.word().toLowerCase().trim();

            char[] display = new char[chosenWord.length()];
            Arrays.fill(display, '_');
            Set<Character> guessedLetters = new HashSet<>();

            out.println(HANGMAN_FRAMES[0]);
            out.println("Word: " + new String(display));
            start = System.nanoTime();
            out.println("INPUT_GUESS");

            while (wrongAttempts < maxAttempts && new String(display).contains("_")) {
                out.println("Guessed so far: " + guessedLetters);
                out.println();
                out.println("Enter your guess client!:(single character)");
                out.println();
                String guessByClient = in.readLine();
                if (guessByClient == null || guessByClient.isBlank()) continue;

                if (guessByClient.trim().equalsIgnoreCase("HINT")) {
                    if (hintsUsed >= MAX_HINTS) {
                        out.println("No hints remaining!");
                        out.println();
                    } else {

                        List<Integer> unrevealed = new ArrayList<>();
                        for (int i = 0; i < chosenWord.length(); i++)
                            if (display[i] == '_') unrevealed.add(i);
                        int idx = unrevealed.get(new Random().nextInt(unrevealed.size()));
                        display[idx] = chosenWord.charAt(idx);
                        hintsUsed++;
                        hintPenalty += 5;
                        out.println("Hint used! (" + hintsUsed + "/" + MAX_HINTS + ") — -5 points penalty");
                        out.println();
                        out.println("Word: " + new String(display));
                        out.println();
                    }
                    continue;
                }

                if (guessByClient.trim().equalsIgnoreCase("PLOTHINT")) {
                    if (plotHintsUsed >= MAX_PLOT_HINTS) {
                        out.println("No plot hints remaining!");
                        out.println();
                    } else if (entry.imdbId() == null) {
                        out.println("No plot hint available for this word.");
                        out.println();
                    } else {
                        String plot = OmdbClient.fetchPlotByImdbId(entry.imdbId(), entry, wso);
                        if (plot == null) {
                            out.println("Plot hint unavailable right now.");
                            out.println();
                        } else {
                            plotHintsUsed++;
                            hintPenalty += PLOT_HINT_PENALTY;
                            out.println("Plot hint: " + plot + " (-" + PLOT_HINT_PENALTY + " points)");
                            out.println();
                        }
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
                    out.println();
                    out.println("Word: " + new String(display));
                    out.println();
                    out.println("Wrong attempts: " + wrongAttempts + "/" + maxAttempts);
                    out.println();
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
                out.println();
                out.println("Wrong attempts: " + wrongAttempts + "/" + maxAttempts);
                out.println();
                out.println(HANGMAN_FRAMES[wrongAttempts]);
            }

            end = System.nanoTime();
            if (new String(display).equals(chosenWord)) {
                score = calculateScore(wrongAttempts, start, end, hintPenalty,maxAttempts);
                out.println("Congratulations...");
                out.println();
                dao.updatePlayerStats(username, score, 1);
                return new PlayerResult(username, score, Status.WIN, wrongAttempts, (int) ((end - start) / 1_000_000_000L));
            } else {
                out.println("Sorry...");
                out.println();
                out.println("The right word is,"+chosenWord);
                out.println();
                dao.updatePlayerStats(username, 0, 0);
            }

        } catch (SocketTimeoutException | SocketException e) {
            logger.log(Level.WARNING, "Client timed out or disconnected: {0}", username);
            clientDisconnectHandler.handleClientDisconnect(out);
            return new PlayerResult(username, 0, Status.NOTHING, wrongAttempts,
                    (int) ((end - start) / 1_000_000_000L));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error during game for user: " + username, e);
            return new PlayerResult(username, 0, Status.NOTHING, wrongAttempts,
                    (int) ((end - start) / 1_000_000_000L));
        }

        return new PlayerResult(username, score, Status.LOSE, wrongAttempts, (int) ((end - start) / 1_000_000_000L));
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

    public int calculateScore(int wrongAttempts, long start, long end,int hintPenality,int maxAttempts) {
        long elapsedSeconds = (end - start) / 1_000_000_000L;
        return ((maxAttempts - wrongAttempts) * 10) + (int) Math.max(0L, 60L - elapsedSeconds)-hintPenality;
    }


    private int parseChoice(String line, int maxOptions) {
        if (line == null) return 0;
        try {
            int choice = Integer.parseInt(line.trim());
            return (choice >= 1 && choice <= maxOptions) ? choice - 1 : 0;
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Invalid category input ''{0}'' — defaulting to first category", line);
            return 0;
        }
    }
}