package service;

import model.PlayerResult;
import model.WaitingPlayer;
import util.LeaderboardPrinter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SingleModeSession  implements Session{

    private final WaitingPlayer waitingPlayer;
    private final HangmanGameEngine hangmanGameEngine=new HangmanGameEngine();
    private final Logger logger=Logger.getLogger("Single mode Session Logger");
    private final ExecutorService executor;
    public SingleModeSession(WaitingPlayer waitingPlayer, ExecutorService executor) {
        this.waitingPlayer = waitingPlayer;
        this.executor = executor;
    }

    @Override
    public void run(){
        PlayerResult result=hangmanGameEngine.run(waitingPlayer);

        try(PrintWriter out= new PrintWriter(new OutputStreamWriter(waitingPlayer.getSocket().getOutputStream()),true)){
            out.println("Match Over");
            out.println("Your score: "+result.getScore());
            LeaderboardPrinter.print(out);
            out.println("Ended");
        }catch (IOException e){
            logger.log(Level.SEVERE,e.getMessage());
        }
    }
}
