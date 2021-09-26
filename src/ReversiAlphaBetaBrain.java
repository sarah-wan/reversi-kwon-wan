// ModelAI Assignment - Reversi
// Topic: Adversarial  Search
// File: ReversiGreedyBrain.java
// Authors: ***

import vitro.*;
import vitro.grid.*;
import java.awt.*;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;

import static vitro.util.Groups.first;

public class ReversiAlphaBetaBrain implements Agent<Reversi.Player> {
    private Reversi model;
    private int myTeam;
    private HashMap<Integer, Reversi.Player> playerHashMap = new HashMap<>();

    ReversiAlphaBetaBrain(Reversi model, int team) {
        this.model = model;
        myTeam = team;

        for (Integer teamNum : model.teams()) {
            playerHashMap.put(teamNum, model.createPlayer(teamNum));
        }
    }

    public Action choose(Reversi.Player actor, Set<Action> options) {
        // If all we can do is pass, go for it.
        if (options.size() == 1) {
            return first(options);
        }

        Action bestAction = null;
        int bestScore = Integer.MIN_VALUE;
        int minScore = Integer.MAX_VALUE;

        // Use maxvalue for alpha and min for beta to indicate we don't know any scores yet
        int alpha = Integer.MAX_VALUE;
        int beta = Integer.MIN_VALUE;
        for (Action a : options) {
            if (!(a instanceof Reversi.Move)) { continue; }
            int score = minimax(actor, a, alpha, beta);
            if (score > bestScore) {
                bestScore = score;
                bestAction = a;
            }
            if (score < minScore) {
                minScore = score;
            }
            // update alpha and beta values
            alpha = Integer.min(alpha, minScore);
            beta = Integer.max(beta, bestScore);
        }

        return bestAction;
    }

    public int minimax(Reversi.Player player, Action option, int alpha, int beta) {
    	if(option instanceof Reversi.Pass)
    	{
    		// If a pass, suppress the output so we don't spam the console
    		// StackOverflow post (https://stackoverflow.com/questions/8363493/hiding-system-out-print-calls-of-a-class)
    		PrintStream stdout = System.out;
    		System.setOut(new PrintStream(new OutputStream(){
    		    public void write(int b) {}
		    }));
    		option.apply();
    		System.setOut(stdout);
    	}
    	else
    		option.apply();

        if (model.done()) {
            option.undo();
            return evalLeaf();
        }

        Reversi.Player nextPlayer = playerHashMap.get(model.team());
        Set<Action> nextMoves = nextPlayer.actions();

        int bestScore = Integer.MIN_VALUE;
        int minScore = Integer.MAX_VALUE;
        
        boolean isMaxNode = myTeam == player.team();

        for (Action a : nextMoves) {
            if (!(a instanceof Reversi.Move) && nextMoves.size() > 1) { continue; }
            int score = minimax(nextPlayer, a, alpha, beta);
            
            // Early return through alpha beta pruning
            if (isMaxNode && score > alpha)
            	break;
            else if (!isMaxNode && score < beta)
            	break;
            
            if (score > bestScore) {
                bestScore = score;
            }
            if (score < minScore) {
                minScore = score;
            }
            
            // update alpha and beta values
            alpha = Integer.min(alpha, minScore);
            beta = Integer.max(beta, bestScore);
        }

        option.undo();
        return isMaxNode ? bestScore : minScore;
    }

    public int evalLeaf() {
        int evaluation = 0;
        for (Map.Entry<Integer,Integer> score : model.scores().entrySet()) {
            if (score.getKey() == myTeam) { evaluation += score.getValue(); }
            else evaluation -= score.getValue();
        }

        return evaluation;
    }
};
