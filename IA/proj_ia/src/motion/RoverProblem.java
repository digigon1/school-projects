package motion;

import static java.lang.Math.*;
import searchalgorithm.Node;
import searchproblem.*;
import java.util.Iterator;

public class RoverProblem extends InformedSearchProblem {
	
	int counter;

	RoverProblem(RoverState initial, RoverState goal) {
		super(initial,goal);
	}
	
	public double heuristic(Node n) {
		RoverState curr = (RoverState) n.getState();

		Iterator<State> it = goalStates.iterator();

		if(it.hasNext()){

			/*counter++;
			if(counter%30 == 0){
				System.out.println("entered");
				counter = 0;
			}*/

			RoverState goal = (RoverState) it.next();

			Terrain t = goal.getTerrain();

			int goal_x = goal.getCoordX(), goal_y = goal.getCoordY();

			int goal_height = t.getHeight(goal_x, goal_y); //TODO

			int x = curr.getCoordX(), y = curr.getCoordY();

			int height = t.getHeight(x, y); //TODO

			int mult = 1;
			switch (t.getTerrainType(x, y)) {
				case ROCK: mult++;
				case SAND: mult++;
				case PLAIN: mult++;
				default: break;
			}

			if(Math.abs(goal_height-height) > 10)
				mult += 5;

			return Math.sqrt(((goal_x-x)*(goal_x-x))+((goal_y-y)*(goal_y-y))+((goal_height-height)*(goal_height-height)));
			
			/*if((goal_y-y) == 0 || (goal_x-x) == 0)
				return 0.0;
			return (goal_height-height);//*/
		}
		
		return 0;
	}
	
}
