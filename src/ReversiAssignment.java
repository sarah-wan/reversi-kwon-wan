// ModelAI Assignment - Reversi
// Topic: Adversarial  Search
// File: Reversi.java
// Authors: ***

import vitro.*;
import vitro.grid.*;

public class ReversiAssignment extends Host {
	
	private static final long serialVersionUID = 1L;
	//number of moves AHEAD the heuristic search will consider
	public static int DEPTH_HEURISTIC_SEARCH = 3;

	public static void main(String[] args) {
		new ReversiAssignment();
	}

	public ReversiAssignment() {

		Reversi model                   = new Reversi(8, 8);
		SequentialController controller = new SequentialController(model);
		ReversiView view                = new ReversiView(model, controller, 640, 480, new ColorScheme());


		Reversi.Player black = model.createPlayer(Reversi.BLACK);
		Reversi.Player white = model.createPlayer(Reversi.WHITE);

		model.actors.add(black);
		model.actors.add(white);

		controller.bind(black, new ReversiHeuristicBrain(model, Reversi.BLACK, DEPTH_HEURISTIC_SEARCH));
		//controller.bind(black, new ReversiAlphaBetaBrain(model, Reversi.BLACK));
		//controller.bind(white, new ReversiAlphaBetaBrain(model, Reversi.WHITE));
		//controller.bind(black, new ReversiMinimaxBrain(model, Reversi.BLACK));
		//controller.bind(white, new ReversiMinimaxBrain(model, Reversi.WHITE));
		//controller.bind(black, new ReversiGreedyBrain());
		controller.bind(white, new ReversiGreedyBrain());

		show(view);
	}
}
