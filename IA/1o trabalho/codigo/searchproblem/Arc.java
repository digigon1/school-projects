package searchproblem;

final public class Arc {
	final private State source;
	final private State destination;
	final private Object action;
	final private double c;

	public Arc(State source, State destination, Object action ) {
		this.source = source;
		this.destination = destination;
		this.action = action;
		this.c = 1.0;
	}

	public Arc(State source, State destination, Object action, double c) {
		this.source = source;
		this.destination = destination;
		this.action = action;
		this.c = c;
	}
	
	public State getParent() {
		return source;
	}
	public State getChild() {
		return destination;
	}
	
	public double getStepCost() {
		return c;
	}
	
	public Object getAction() {
		return action;
	}
	
	public String toString() {
		return source.toString() + "(" + c + ")->" + destination.toString();
	}
}
