
package net;

import dao.PlayerStatsDAO;
import dao.WordsStatsDAO;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ClientHandler implements Runnable {
    private Socket socket;
    private PlayerStatsDAO dao = new PlayerStatsDAO();
    private WordsStatsDAO wso=new WordsStatsDAO();
    private static final String[] hangman = {
    "   +---+\n   |   |\n       |\n       |\n       |\n       |\n  =========",
    "   +---+\n   |   |\n   O   |\n       |\n       |\n       |\n  =========",
    "   +---+\n   |   |\n   O   |\n   |   |\n       |\n       |\n  =========",
    "   +---+\n   |   |\n   O   |\n  /|   |\n       |\n       |\n  =========",
    "   +---+\n   |   |\n   O   |\n  /|\\  |\n       |\n       |\n  =========",
    "   +---+\n   |   |\n   O   |\n  /|\\  |\n  /    |\n       |\n  =========",
    "   +---+\n   |   |\n   O   |\n  /|\\  |\n  / \\  |\n       |\n  ========="
         };
    private char[] display;
    private static final Logger clinetHandlerLog =Logger.getLogger("Client Handler");
    private static final int MAX_ATTEMPTS=7;
    public ClientHandler(Socket socket) {
        this.socket = socket;
        
    }

    @Override
    public void run() {
        try (
            BufferedReader in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out=new PrintWriter(new OutputStreamWriter(socket.getOutputStream()),true)) {
            String username =in.readLine();
            out.println("Welcome " + username + "! Let's play Hangman.");
            out.println("Choose your category\t1.Comic-Series\t2.Thriller-Movies\t3.SciFi-Movies");
            out.println("Choose 1 or 2 or 3:");
            int choice=Integer.parseInt(in.readLine());
            String chosenWord =wso.getWordUnderGivenCategory(choice);
            display=new char[chosenWord.length()];
            for(int i=0;i<chosenWord.length();i++){
                this.display[i]='_';
            }
            int wrongAttempts=0; 
            int score = 0;

            while (wrongAttempts<MAX_ATTEMPTS && new String(display).contains("_")) {
                 out.println("Enter your guess client!:(single character)");
                         String guessByClient=in.readLine();
                         if(guessByClient==null || guessByClient.isEmpty())continue;
                         char guess=guessByClient.charAt(0);
                         boolean found=false;
                         for(int i=0;i<chosenWord.length();i++){
                            if (chosenWord.charAt(i)==guess && display[i]=='_') {
                                display[i]=guess;
                                found=true;
                            }
                         }
                         if(!found)wrongAttempts++;
                         out.println("Word: "+new String(display));
                         out.println("Wrong attempts: "+wrongAttempts+"/"+MAX_ATTEMPTS);
                         if (wrongAttempts==7) {
                            //pass
                         }else out.println(hangman[wrongAttempts]);
                    }  

                     if (new String(display).equals(chosenWord)) {
                        out.println("Congratulation "+username+" have guessed the word "+chosenWord);
                        score=calculateScore(wrongAttempts);
                        out.println("Game ends you got score="+score);
                    }else{
                        out.println("Sorry you have lost the game the actual word "+chosenWord);
                        out.println("Game ends");
                    }
                     dao.updatePlayerStats(username, score);
            }
         catch (Exception e) {
            clinetHandlerLog.log(Level.SEVERE,e.getMessage());
        }
    }
    public int calculateScore(int noofAttempts){
        return (MAX_ATTEMPTS-noofAttempts)*10;
    }
}
