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

public class ReversiHeuristicBrain implements Agent<Reversi.Player> {
    private Reversi model;
    private int myTeam;
    private int depth;
    private HashMap<Integer, Reversi.Player> playerHashMap = new HashMap<>();
    private int totalStates = 0;

    ReversiHeuristicBrain(Reversi model, int team, int depth) {
        this.model = model;
        myTeam = team;
        this.depth = depth;

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
        double bestScore = Double.NEGATIVE_INFINITY;
        double minScore = Double.POSITIVE_INFINITY;

        // Use maxvalue for alpha and min for beta to indicate we don't know any scores yet
        double alpha = Double.POSITIVE_INFINITY;
        double beta = Double.NEGATIVE_INFINITY;
        for (Action a : options) {
            if (!(a instanceof Reversi.Move)) { continue; }
            double score = minimax(actor, a, alpha, beta, depth);
            if (score > bestScore) {
                bestScore = score;
                bestAction = a;
            }
            if (score < minScore) {
                minScore = score;
            }
            // update alpha and beta values
            alpha = Double.min(alpha, minScore);
            beta = Double.max(beta, bestScore);
        }

        System.out.println("Player " + myTeam + ": " + totalStates + " states evaluated");
        return bestAction;
    }

    public double minimax(Reversi.Player player, Action option, double alpha, double beta, int depth) {
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

        if (depth == 0 || model.done()) {
            option.undo();
            return evalLeaf();
        }

        Reversi.Player nextPlayer = playerHashMap.get(model.team());
        Set<Action> nextMoves = nextPlayer.actions();

        double bestScore = Double.NEGATIVE_INFINITY;
        double minScore = Double.POSITIVE_INFINITY;

        boolean isMaxNode = myTeam == player.team();

        for (Action a : nextMoves) {
            if (!(a instanceof Reversi.Move) && nextMoves.size() > 1) { continue; }
            double score = minimax(nextPlayer, a, alpha, beta, depth - 1);

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
            alpha = Double.min(alpha, minScore);
            beta = Double.max(beta, bestScore);
        }

        option.undo();
        return isMaxNode ? bestScore : minScore;
    }

    public double evalLeaf() {
        totalStates++;

        //heuristicEval is a weighted avg of heuristics
        double heuristicEval = mobilityEval();
        return heuristicEval;
    }

    public double scoreEval() {
        int evaluation = 0;
        int total = 0;

        for (Map.Entry<Integer,Integer> score : model.scores().entrySet()) {
            total += score.getValue();
            if (score.getKey() == myTeam) {
                evaluation += score.getValue();
            } else evaluation -= score.getValue();
        }

        return evaluation / total;
    }

    public double stabilityEval() {
        //write stability heuristic here
        return 5;
    }

    public double mobilityEval() {
        // Number of moves for me minus number of moves for each opponent
    	int heur = 0;
    	for (Map.Entry<Integer, Reversi.Player> entry : playerHashMap.entrySet()) {
    		int change = entry.getValue().actions().size();
    		if (entry.getKey() == myTeam) {
    			heur += change;
    		} 
    		else {
    			heur -= change;
    		}
    	}
        return heur;
    }

    public double anotherEval() {
        return 5;
    }

    public double anotheranotherEval() {
        return 5;
    }

    public double yetanotherEval() {
        return 5;
    }

    public double lastEval() {
        return 5;
    }
};
