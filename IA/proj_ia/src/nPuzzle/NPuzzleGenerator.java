package nPuzzle;

import java.util.*;
import searchproblem.Arc;

public class NPuzzleGenerator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Random rdm = new Random();
		
		int depth = rdm.nextInt(100);
		
		Number s[][] = { {1,2,3,4}, {5,6,7,8}, {9,10,11,12}, {13,14,15,null} };
		NPuzzleState init = new NPuzzleState(s,s.length);
		
		System.out.println(depth);
		
		for( int i=0; i < depth; i++ ) {
			List<Arc> moves = init.successorFunction();
			System.out.println( moves.size() );
			init.applyOperator(moves.get(rdm.nextInt(moves.size())).getAction());
		}

		System.out.println( init );
	}

}
