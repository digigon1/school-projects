package vacuumworld;
import searchalgorithm.*;

public class VacuumTest {

	public static void main(String[] args) {
		boolean[] world = {true, true, false, false, true};
		VacuumState init = new VacuumState(world,1,5);

		VacuumProblem prob = new VacuumProblem(init);
		//SearchAlgorithm alg = new BreadthFirstSearch(prob);
		SearchAlgorithm alg = new UniformCostSearch(prob);
		//SearchAlgorithm alg = new DepthFirstSearch(prob);
		System.out.println(alg.searchSolution().getPath());	
		System.out.println(alg.getMetrics());
	}

}
