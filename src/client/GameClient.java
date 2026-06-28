package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class GameClient {

    public static void main(String[] args) {

        try (Socket socket = new Socket("localhost", 8080);
             PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner sc = new Scanner(System.in)) {

            String serverMessage;

            outer:
            while ((serverMessage = in.readLine()) != null) {

                switch (serverMessage) {



                   case "INPUT_USERNAME" -> {
                        System.out.print("Enter your username: ");
                        out.println(sc.nextLine());
                    }

                    case "INPUT_MODE" -> {
                        for (int i = 0; i < 8; i++) System.out.println(in.readLine());
                        System.out.print("> ");
                        out.println(sc.nextLine());
                    }

                     case "INPUT_PASSWORD_NEW", "INPUT_PASSWORD_AUTH" -> {
                        System.out.println(in.readLine());
                        System.out.print("Password (hidden): ");
                        String pw = readPassword(sc);
                        out.println(pw);
                    }

                    case "AUTH_SUCCESS" -> {
                        System.out.println(in.readLine());
                    }

                    case "AUTH_FAILED" -> {
                        System.out.println("⚠  " + in.readLine());
                    }
                    case "AUTH_BLOCKED" -> {
                        System.out.println("✗  " + in.readLine());
                    }

                      case "INPUT_CATEGORY" -> {
                        for (int i = 0; i < 6; i++) System.out.println(in.readLine());
                        System.out.print("> ");
                        out.println(sc.nextLine());
                    }

                    case "CHAT_SENT"->{

                    }
                    case "INPUT_GUESS" -> {
                        banner("Game started! Good luck!");
                    }


                    case "WAITING" -> {
                        System.out.println(in.readLine());
                    }


                    case "MATCH_FOUND" -> {
                        System.out.println(in.readLine());
                        banner("Opponent found! Prepare yourself!");
                    }


                    default -> {
                        System.out.println(serverMessage);

                        if (serverMessage.contains("Enter your guess client!:(single character)")) {
                            System.out.print("Your guess (or type HINT /CHAT:Message): ");
                            String guess = sc.nextLine().trim();
                            out.println(guess.isEmpty() ? " " : guess);
                        }

                        if (serverMessage.contains("Ended")) {
                            System.out.println("Thanks for playing! Goodbye.");
                            break outer;
                        }
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("Connection error: " + e.getMessage());
        }
    }

    /**
     * Reads a password without echoing characters if a real terminal is available
     * (i.e. when run from a command prompt, not an IDE console).
     * Falls back to plain Scanner.nextLine() in IDE environments where
     * System.console() returns null.
     */
    private static String readPassword(Scanner sc) {
        Console console = System.console();
        if (console != null) {
            char[] pw = console.readPassword();
            return pw != null ? new String(pw) : "";
        }
        System.out.print("[IDE mode — password visible]: ");
        return sc.nextLine();
    }

    private static void banner(String text) {
        String bar = "═".repeat(text.length() + 4);
        System.out.println("╔" + bar + "╗");
        System.out.println("║  " + text + "  ║");
        System.out.println("╚" + bar + "╝");
    }
}