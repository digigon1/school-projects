package vacuumworld;

import searchalgorithm.Node;
import searchproblem.*;

public class VacuumProblem extends InformedSearchProblem {

	VacuumProblem(VacuumState initial) {
		super(initial);
	}
	
	public boolean goalTest(State n) {
		if(goalStates != null)
			return super.goalTest(n);
		else if( n instanceof VacuumState )
			return !((VacuumState)n).isDirty();
		else
			return false;
	}

	public double heuristic(Node n) {
		return ((VacuumState)n.getState()).getDirty();
	}
}
