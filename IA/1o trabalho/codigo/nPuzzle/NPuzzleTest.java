package nPuzzle;

import java.util.*;

import searchalgorithm.*;
import searchproblem.*;

public class NPuzzleTest {

	public static void main(String[] args) {

//		Number s[][] = { {7,2,4}, {5,null,6}, {8,3,1} };
//		Number f[][] = { {1,2,3}, {4,null,5}, {6,7,8} };

		Number s[][] = { {7,6,14,4}, {2,3,8,15}, {1,9,10,11}, {5,null,13,12} };
		Number f[][] = { {1,2,3,4}, {5,6,7,8}, {9,10,11,12}, {13,14,15,null} };

		NPuzzleState init = new NPuzzleState(s,s.length);
		NPuzzleState goal = new NPuzzleState(f,f.length);

		
		
		NPuzzleProblem prob = new NPuzzleProblem(init,goal);
//		SearchAlgorithm alg = new UniformCostSearch(prob);
//		SearchAlgorithm alg = new BreadthFirstSearch(prob);
//		SearchAlgorithm alg = new DepthFirstSearch(prob);
		SearchAlgorithm alg = new AStarSearch(prob);
		
		Node n = alg.searchSolution();
		if( n != null)
			System.out.println(n.getPath());
		System.out.println(alg.getMetrics());
	}

}
