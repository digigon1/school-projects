package motion;

import java.util.ArrayList;
//import java.util.Arrays;
import java.util.List;

import motion.Terrain.TerrainType;

//import static java.lang.Math.*;

import searchproblem.*;


public class RoverState extends State implements Cloneable {
	
	public final int PLAIN_FACTOR = 1;
	public final int SAND_FACTOR = 2;
	public final int ROCK_FACTOR = 3;

	public final int MAX_HEIGHT = 10;
	private int x, y, height;
	private BitmapTerrain terrain; //final?
	public enum RoverOperator{
		N, E, S, W, NE, SE, SW, NW
	}
	
	public RoverState(int startx, int starty, BitmapTerrain t){
		terrain=t;
		x=startx;
		y=starty;
		height=t.getHeight(x,y);
	}

	//TEST
	public BitmapTerrain getTerrain(){
		return terrain;
	}
	
	public List<Arc> successorFunction(){
		List<Arc> children = new ArrayList<Arc>(8);
		for(RoverOperator action : RoverOperator.values() ) {
			if( applicableOperator(action) )
				children.add(successorState(action));
		}
		return children;
	};
	
	public Arc successorState(Object action){
		RoverState child = (RoverState) this.clone();
		return new Arc(this,child,action,child.applyOperator(action));
	};
	
	//Garantir que nao sai fora do mapa e nao excede a diferença de altura
	public boolean applicableOperator(Object action){
		if ( action instanceof RoverOperator) {
			return true;
		} else {
			return false;
		}
	}; 
	public double applyOperator(Object action){
		RoverOperator op = (RoverOperator) action;
		double cost = 0.0;
		
		switch (op) {
		case N: 
			cost = operator(0, -1); break;
		case E: 
			cost = operator(1, 0); break;
		case S: 
			cost = operator(0, 1); break;
		case W: 
			cost = operator(-1, 0); break;
		case NE: 
			cost = operator(1, -1); break;
		case SE: 
			cost = operator(1, 1); break;
		case SW: 
			cost = operator(-1, 1); break;
		case NW: 
			cost = operator(-1, -1); break;
		}
		return cost;
	}

	private double operator(int deltaX, int deltaY){
		double cost=0;
		int new_x = x + deltaX;
		int new_y = y + deltaY;
		if(canMove(new_x,new_y)){
			cost=getCost(new_x,new_y);
			x=new_x;
			y=new_y;
			height = terrain.getHeight(x, y);
		}
		return cost;
	}

	private boolean canMove(int i, int j){
		return i<terrain.getHorizontalSize() && i >= 0 
			&& j<terrain.getVerticalSize() && j >= 0 
			&& Math.abs((terrain.getHeight(i,j)-height))<=MAX_HEIGHT;
	}

	private double getCost(int i, int j){
		int final_height = terrain.getHeight(i,j); 
		double normal_cost = Math.sqrt(((i-x)*(i-x))+((j-y)*(j-y))+((final_height-height)*(final_height-height)));
		double factor = Math.pow(Math.E,Math.sqrt(Math.abs(final_height-height)));

		if(terrain.getTerrainType(i,j)==TerrainType.SAND)
			return normal_cost*factor*SAND_FACTOR;
		else if(terrain.getTerrainType(i,j)==TerrainType.ROCK)
			return normal_cost*factor*ROCK_FACTOR;
		else
			return normal_cost*factor*PLAIN_FACTOR;
	};

	public Object clone() {
		return new RoverState(x,y,terrain);
	}

	@Override
	public int hashCode() {
		final int PRIME = 113;
		int result = 1;
		result = PRIME * result + x;
		result = PRIME * result + y;
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
		final RoverState other = (RoverState) obj;
		if (this.x != other.x || this.y != other.y)
			return false;
		return true;
	}

	public int getCoordX() {
		return x;
	}

	public int getCoordY() {
		return y;
	}
}
