package vacuumworld;

import java.util.*;
import searchproblem.*;

public class VacuumState extends State implements Cloneable {

	boolean[] dirt;
	int location;
	int size;
	int dirty;
	
	VacuumState(boolean[] dirt, int location, int size) {
		this.dirt = dirt.clone();
		this.location = location;
		this.size = size;
		dirty = 0;
		for (int i=0; i < size; i++)
			if( dirt[i] ) dirty++;
	}
	
	private VacuumState(boolean[] dirt, int location, int size, int dirty) {
		this.dirt = dirt.clone();
		this.location = location;
		this.size = size;
		this.dirty = dirty;
	}
	
	@Override
	public Object clone() {
		return new VacuumState(dirt,location,size,dirty);
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + Arrays.hashCode(dirt);
		result = PRIME * result + location;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final VacuumState other = (VacuumState) obj;
		if (this.location != other.location || this.dirty != other.dirty )
			return false;
		if (!Arrays.equals(dirt, other.dirt))
			return false;
		return true;
	}

	public List<Arc> successorFunction() {
		List<Arc> children = new ArrayList<Arc>(3);
		for(VacuumOperator action : VacuumOperator.values() ) {
			if( applicableOperator(action) )
				children.add(successorState(action));
		}
		return children;
	}

	public boolean applicableOperator(Object action) {
		if ( action instanceof VacuumOperator) {
			return true;
		} else {
			return false;
		}
	}

	public Arc successorState(Object action) {
		VacuumState child = (VacuumState) this.clone();
		return new Arc(this,child,action,child.applyOperator(action));
	}
	
	public double applyOperator(Object action) {
		
		VacuumOperator op = (VacuumOperator) action;

		switch (op) {
		case LEFT: 
			leftOperator();
			break;
		case RIGHT: 
			rightOperator();
			break;
		case SUCK: 
			suckOperator();
			break;
		}
		return 1.0;
	}
	
	
	public boolean isDirty() {
		return dirty > 0;
	}
	
	protected void leftOperator() {
		if( location > 0)
			location--;
	}
	
	protected void rightOperator() {
		if( location < size - 1)
			location++;
	}
	
	protected void suckOperator() {
		if( dirt[location]) dirty--;
		dirt[location] = false;
	}

}
