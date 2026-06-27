package service;

import model.PlayerResult;
import model.Status;
import model.WaitingPlayer;
import util.LeaderboardPrinter;

import java.io.*;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SingleModeSession  implements Session{

    private final WaitingPlayer waitingPlayer;
    private final HangmanGameEngine hangmanGameEngine;
    private final Logger logger=Logger.getLogger("Single mode Session Logger");
    private final LeaderboardPrinter leaderboardPrinter;
    private final MatchHistoryService matchHistoryService;
    public SingleModeSession(WaitingPlayer waitingPlayer, HangmanGameEngine hangmanGameEngine, LeaderboardPrinter leaderboardPrinter, MatchHistoryService matchHistoryService) {
        this.waitingPlayer = waitingPlayer;
        this.hangmanGameEngine = hangmanGameEngine;
        this.leaderboardPrinter = leaderboardPrinter;
        this.matchHistoryService = matchHistoryService;
    }

    @Override
    public void run(){
        try(PrintWriter out= new PrintWriter(new OutputStreamWriter(waitingPlayer.getSocket().getOutputStream()),true);
            BufferedReader in=new BufferedReader(new InputStreamReader(waitingPlayer.getSocket().getInputStream()))){
            ClientDisconnectHandler clientDisconnectHandler=new ClientDisconnectHandler(out,null);
            PlayerResult result=hangmanGameEngine.run(waitingPlayer,in,out,null,clientDisconnectHandler);
            if (clientDisconnectHandler.isDisconnected()) return;
            if (result.status() == Status.NOTHING){
                out.println("A server error occurred. Disconnecting.");
                out.println("Ended");
                return;
            }
            out.println("Match Over");
            out.println("Your score: "+result.score());
            matchHistoryService.saveSinglePlayerSession(waitingPlayer.getId(), result, result.status() == Status.WIN);
            leaderboardPrinter.print(out);

            out.println("Ended");
        }catch (IOException e){
            logger.log(Level.SEVERE,e.getMessage());
        }
    }
}
