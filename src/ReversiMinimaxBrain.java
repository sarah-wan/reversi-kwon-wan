// ModelAI Assignment - Reversi
// Topic: Adversarial  Search
// File: ReversiGreedyBrain.java
// Authors: ***

import vitro.*;
import vitro.grid.*;
import java.awt.*;
import java.util.*;

public class ReversiMinimaxBrain implements Agent<Reversi.Player> {
    private Reversi model;
    private int myTeam;
    private HashMap<Integer, Reversi.Player> playerHashMap = new HashMap<>();

    ReversiMinimaxBrain(Reversi model, int team) {
        this.model = model;
        myTeam = team;

        for (Integer teamNum : model.teams()) {
            playerHashMap.put(teamNum, model.createPlayer(teamNum));
        }
    }

    public Action choose(Reversi.Player actor, Set<Action> options) {

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

        return bestAction;
    }

    public int minimax(Reversi.Player player, Action option) {
        if ((option instanceof Reversi.Pass) && model.done()) {
            return evalLeaf();
        }

        option.apply();
        Reversi.Player nextPlayer = playerHashMap.get(model.team());
        Set<Action> nextMoves = nextPlayer.actions();

        if (myTeam == player.team()) {
            int bestScore = Integer.MIN_VALUE;

            for (Action a : nextMoves) {
                if (!(a instanceof Reversi.Move)) { continue; }
                int score = minimax(nextPlayer, a);
                if (score > bestScore) {
                    bestScore = score;
                }
            }

            option.undo();
            return bestScore;
        } else {
            int minScore = Integer.MAX_VALUE;

            for (Action a : nextMoves) {
                if (!(a instanceof Reversi.Move)) { continue; }
                int score = minimax(nextPlayer, a);
                if (score < minScore) {
                    minScore = score;
                }
            }

            option.undo();
            return minScore;
        }
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
