// ModelAI Assignment - Reversi
// Topic: Adversarial  Search
// File: ReversiGreedyBrain.java
// Authors: ***

import vitro.*;
import vitro.grid.*;

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
    private int totalMoves = 0;

    ReversiHeuristicBrain(Reversi model, int team, int depth) {
        this.model = model;
        myTeam = team;
        this.depth = depth;

        for (Integer teamNum : model.teams()) {
            playerHashMap.put(teamNum, model.createPlayer(teamNum));
        }
    }

    public Action choose(Reversi.Player actor, Set<Action> options) {
        totalMoves++;

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

        double heuristicEval= 0;

        heuristicEval += 200 * stableCorners();

        if (totalMoves > (model.height * model.width)/ 4.0 ) { //last half of game
            heuristicEval += 125 * scoreEval();
        } else {
            heuristicEval += 75 * scoreEval(); //first half of game
        }


        if (totalMoves > (model.height * model.width)/ 6.0) { // last 2/3 of game
            heuristicEval += 20 * mobilityEval();

        } else { //very early game strategy: first 1/3 of game
            heuristicEval += 60 * mobilityEval();

            if (mobilityEval() > 0) {
                heuristicEval += 40 * frontierEval();
            }
            heuristicEval += 15 * xandC();
        }

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

        return ((double) evaluation) / total;
    }

    public double frontierEval() {
        int myFrontier = 0;
        int otherFrontier = 0;
        int[][] deltas = {{-1,1}, {-1,0}, {-1,-1}, {0,1}, {0,-1},{1,1}, {1,0}, {1,-1}};
        for (Map.Entry<vitro.Actor, vitro.grid.Location> entry : model.locations.entrySet()) {
            for(int i = 0; i < deltas.length; ++i) {
                int nx = entry.getValue().x + deltas[i][0];
                int ny = entry.getValue().y + deltas[i][1];
                if (nx >= 0 && nx < this.model.width && ny >= 0 && ny < this.model.height) {
                    Location neighbor = new Location(this.model, nx, ny);
                    if (model.actorAt(neighbor) == null) {
                        if (((Reversi.Piece) entry.getKey()).team == myTeam) {
                            myFrontier++;
                        } else {
                            otherFrontier++;
                        }
                    }
                }
            }
        }

        if (otherFrontier + myFrontier == 0) return 0;

        return myFrontier < otherFrontier ? ((double) myFrontier) / (myFrontier + otherFrontier) :  - ((double) otherFrontier) / (myFrontier + otherFrontier);
    }

    public double mobilityEval() {
        // Number of moves for me minus number of moves for each opponent
    	int heur = 0;
        int sum = 0;

    	for (Map.Entry<Integer, Reversi.Player> entry : playerHashMap.entrySet()) {
    		int change = entry.getValue().actions().size();
            sum += change;
    		if (entry.getKey() == myTeam) {
    			heur += change;
    		} 
    		else {
    			heur -= change;
    		}
    	}
        return ((double) heur) / sum;
    }

    public double stableCorners() {
        if (model.height < 4) return 0;

        int otherCorners = 0;
        int myCorners = 0;


        if (model.height >= 4) {
            //Corners valued at 50
            Location[] corners = {new Location(model, 0,0), new Location(model, 0,model.width - 1),
                    new Location(model, model.height - 1,0),new Location(model, model.height - 1,model.width - 1)};
            for (Location l : corners) {
                if (model.actorAt(l) == null) continue;

                if (((Reversi.Piece) model.actorAt(l)).team == myTeam) {
                    myCorners += 1;
                } else {
                    otherCorners += 1;
                }
            }
        }

        if (otherCorners + myCorners == 0) return 0;
        return myCorners < otherCorners ? - ((double) otherCorners) / (myCorners + otherCorners) : ((double) myCorners) / (myCorners + otherCorners);
    }

    public double xandC() {

        int myScore = 0;
        int otherScore = 0;

        if (model.height >= 8) {
            //xPiece valued at -2
            Location[] xPiece = {new Location(model, 1,1), new Location(model, 1,model.width - 2),
                    new Location(model, model.height - 2,1),new Location(model, model.height - 2,model.width - 2)};
            //cPiece valued at -1
            Location[] cPiece = {new Location(model, 0,1), new Location(model, 1,0),
                    new Location(model, 0,model.width - 2), new Location(model, 1,model.width - 1),
                    new Location(model, model.height - 1,1), new Location(model, model.height - 2,0),
                    new Location(model, model.height - 1,model.width - 2), new Location(model, model.height - 2,model.width - 1)};

            for (Location l : xPiece) {
                if (model.actorAt(l) == null) continue;

                if (((Reversi.Piece) model.actorAt(l)).team == myTeam) {
                    myScore += -2;
                } else {
                    otherScore += -2;
                }

            }

            for (Location l : cPiece) {
                if (model.actorAt(l) == null) continue;

                if (((Reversi.Piece) model.actorAt(l)).team == myTeam) {
                    myScore += -1;
                } else {
                    otherScore += -1;
                }
            }
        }

        if (myScore + otherScore == 0) return 0;

        return myScore > otherScore ? ((double) otherScore) / (myScore + otherScore) : - ((double) myScore) / (myScore + otherScore);
    }
};
