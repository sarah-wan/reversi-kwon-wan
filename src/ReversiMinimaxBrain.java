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

public class ReversiMinimaxBrain implements Agent<Reversi.Player> {
    private Reversi model;
    private int myTeam;
    private HashMap<Integer, Reversi.Player> playerHashMap = new HashMap<>();
    private int totalStates = 0;

    ReversiMinimaxBrain(Reversi model, int team) {
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
        double bestScore = Integer.MIN_VALUE;

        for (Action a : options) {
            if (!(a instanceof Reversi.Move)) { continue; }
            int score = minimax(actor, a);
            if (score > bestScore) {
                bestScore = score;
                bestAction = a;
            }
        }

        System.out.println("Player " + myTeam + ": " + totalStates + " states evaluated");
        return bestAction;
    }

    public int minimax(Reversi.Player player, Action option) {
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

        for (Action a : nextMoves) {
            if (!(a instanceof Reversi.Move) && nextMoves.size() > 1) { continue; }
            int score = minimax(nextPlayer, a);
            if (score > bestScore) {
                bestScore = score;
            }

            if (score < minScore) {
                minScore = score;
            }
        }

        option.undo();
        return myTeam == player.team() ? bestScore : minScore;
    }

    public int evalLeaf() {
        int evaluation = 0;
        for (Map.Entry<Integer,Integer> score : model.scores().entrySet()) {
            if (score.getKey() == myTeam) { evaluation += score.getValue(); }
            else evaluation -= score.getValue();
        }

        totalStates++;
        return evaluation;
    }
};
