package vacuumworld;

import searchproblem.*;

public class VacuumProblem extends SearchProblem {

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
}
