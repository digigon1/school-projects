package nPuzzle;

import java.util.*;
import searchproblem.*;
import searchalgorithm.*;
import static java.lang.Math.*;

public class NPuzzleProblem extends InformedSearchProblem {
	private HashMap<Object,Integer> coordX = new HashMap<Object,Integer>();
	private HashMap<Object,Integer> coordY = new HashMap<Object,Integer>();;
	
	public NPuzzleProblem(NPuzzleState initial, NPuzzleState goal) {
		super(initial,goal); 
		for(int i=0; i < goal.size ; i++ )
			for(int j=0; j < goal.size; j++) {
				coordX.put(goal.tiles[i][j], i);
				coordY.put(goal.tiles[i][j], j);				
			}
	}
	
	public double heuristic(Node n) {
		double manhattan = 0.0;
		NPuzzleState curr = (NPuzzleState) n.getState();
		for(int i=0; i < curr.size ; i++ )
			for(int j=0; j < curr.size; j++) 
				manhattan += abs( coordX.get(curr.tiles[i][j]) - i) + abs(coordY.get(curr.tiles[i][j]) - j);
		return manhattan;
	}
	
}
