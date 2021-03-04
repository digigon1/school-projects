package searchproblem;
import java.util.*;

public class SearchProblem {

	protected State initial;
	protected Set<State> goalStates;

	public SearchProblem(State initial) {
		this.initial = initial;
		this.goalStates = null;
	}

	public SearchProblem(State initial, Set<State> goals) {
		this.initial = initial;
		this.goalStates = goals;
	}
	
	public SearchProblem(State initial, State goal) {
		this.initial = initial;
		this.goalStates = new HashSet<State>(1);
		this.goalStates.add(goal);
	}
	
	public boolean goalTest(State n) {
		if( goalStates != null )
			return goalStates.contains(n);
		else
			return false;
	}
	
	public State getInitial() {
		return initial;
	}
	
}
