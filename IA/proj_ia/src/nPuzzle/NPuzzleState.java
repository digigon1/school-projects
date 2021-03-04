package nPuzzle;

import java.util.*;

import searchproblem.*;

public class NPuzzleState extends State implements Cloneable {

	public enum NPuzzleOperator {
		LEFT, RIGHT, UP, DOWN
	}

	protected Object[][] tiles;
	protected int size;
	private int blankX;
	private int blankY;
	
	NPuzzleState( Object[][] tiles, int size) {
		this.tiles = tiles.clone();
		this.size = size;
		for( int i=0; i < size; i++) {
			this.tiles[i] = tiles[i].clone();
			for( int j=0; j < size; j++)
				if( tiles[i][j] == null ) {
					blankX=i;
					blankY=j;
					return;
				}
		}
		throw new RuntimeException("Puzzle must have a blank square");
	}
	
	private NPuzzleState( Object[][] tiles, int size, int blankX, int blankY) {
		this.tiles = tiles.clone();
		for( int i=0; i < size; i++)
			this.tiles[i] = tiles[i].clone();
		this.size = size;
		this.blankX = blankX;
		this.blankY = blankY;
	}
	
	@Override
	public Object clone() {
		return new NPuzzleState(tiles,size,blankX,blankY);
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		for( int i=0; i < size ; i++ )
			result = PRIME * result + Arrays.hashCode(tiles[i]);
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
		final NPuzzleState other = (NPuzzleState) obj;
		if( this.blankX != other.blankX || this.blankY != other.blankY )
			return false;
		for( int i=0; i < size; i++)
			if (!Arrays.equals(tiles[i], other.tiles[i]))
				return false;
		return true;
	}	
	
	@Override
	public String toString() {
		String board="[";
		
		for( int i = 0 ; i < size ; i++) {
			board += "[ ";
			for( int j = 0 ; j < size ; j++ )
				if( tiles[i][j] == null )
					board +="b ";
				else
					board += tiles[i][j]+" ";
			board += "]";
		}
		return board+"]";
	}

	public List<Arc> successorFunction() {
		List<Arc> children = new ArrayList<Arc>(4);
		for(NPuzzleOperator action : NPuzzleOperator.values() ) {
			if( applicableOperator(action))
				children.add(successorState(action));
		}
		return children;
	}
	
	public boolean applicableOperator(Object action) {
		if ( !(action instanceof NPuzzleOperator) )
			return false;
		
		NPuzzleOperator op = (NPuzzleOperator) action;
		switch (op) {
		case RIGHT: 
			return blankY > 0;
		case LEFT:
			return blankY < size-1;
		case DOWN:
			return blankX > 0;
		case UP:
			return blankX < size-1;
		}
		return false;
		
	}
	
	public Arc successorState(Object action) {
		NPuzzleState child = (NPuzzleState) this.clone();
		return new Arc(this,child,action,child.applyOperator(action));		
	}
	
	public double applyOperator(Object action) {
		NPuzzleOperator op = (NPuzzleOperator) action;
		switch (op) {
		case LEFT: 
			blankRightOperator();
			break;
		case RIGHT:
			blankLeftOperator();
			break;
		case UP:
			blankDownOperator();
			break;
		case DOWN:
			blankUpOperator();
			break;
		}
		return 1.0;
	}

	private void blankUpOperator() {
		if( blankX > 0) {
			tiles[blankX][blankY] = tiles[blankX-1][blankY];
			tiles[blankX-1][blankY] = null;
			blankX--;
		} 
	}
	
	private void blankDownOperator() {
		if( blankX < size-1) {
			tiles[blankX][blankY] = tiles[blankX+1][blankY];
			tiles[blankX+1][blankY] = null;
			blankX++;
		} 
	}

	private void blankLeftOperator() {
		if( blankY > 0) {
			tiles[blankX][blankY] = tiles[blankX][blankY-1];
			tiles[blankX][blankY-1] = null;
			blankY--;
		} 
	}
	
	private void blankRightOperator() {
		if( blankY < size-1) {
			tiles[blankX][blankY] = tiles[blankX][blankY+1];
			tiles[blankX][blankY+1] = null;
			blankY++;
		} 
	}

}


