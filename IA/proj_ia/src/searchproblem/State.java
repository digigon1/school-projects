package searchproblem;
import java.util.*;

public abstract class State {

	public abstract List<Arc> successorFunction();
	
	public abstract Arc successorState(Object op);

	public abstract double applyOperator(Object op);

	public abstract Object clone();
	
	public abstract int hashCode();
	
	public abstract boolean equals(Object obj);
	
}
