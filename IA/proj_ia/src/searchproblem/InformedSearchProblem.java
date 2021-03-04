package searchproblem;

import searchalgorithm.*;
import java.util.Set;

abstract public class InformedSearchProblem extends SearchProblem implements HeuristicFunction {

	public InformedSearchProblem(State initial) {
		super(initial);
	}

	public InformedSearchProblem(State initial, Set<State> goals) {
		super(initial, goals);
	}

	public InformedSearchProblem(State initial, State goal) {
		super(initial, goal);
	}
	
	abstract public double heuristic(Node n);

}
