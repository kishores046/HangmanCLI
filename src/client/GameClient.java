
package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class GameClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 8080);
             PrintWriter out=new PrintWriter(new OutputStreamWriter(socket.getOutputStream()),true);
             BufferedReader in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner sc = new Scanner(System.in)) {
            System.out.print("Enter username: ");
            String username = sc.nextLine();
            out.println(username);
            System.out.println(in.readLine());
            System.out.println(in.readLine());
            System.out.println(in.readLine());
            int choice=sc.nextInt();
            out.println(choice+"");
            String serverMessage;
            while ((serverMessage=in.readLine())!=null) {
                System.out.println("Server:"+serverMessage);
                if(serverMessage.contains("Enter your guess client!:(single character)")){
                    System.out.print("Your guess:");
                    String guess=sc.nextLine();
                    out.println(guess);
                }
                if (serverMessage.contains("Game ends") || serverMessage.contains("Congratulations")) {
                    break;
                }
            }

        } catch (Exception e) {
            System.out.println("Error connecting to server:"+e.getMessage());
        }
    }
}


