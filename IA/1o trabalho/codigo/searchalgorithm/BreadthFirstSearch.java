package searchalgorithm;
import java.util.*;

import searchproblem.SearchProblem;

public class BreadthFirstSearch implements SearchAlgorithm {

	private TreeSearch graph;
	
	public BreadthFirstSearch(SearchProblem p) {
		Queue<Node> fifo = new LinkedList<Node>();
		graph = new TreeSearch(p,fifo);
	}
	
	public Node searchSolution() {
		return graph.searchSolution();
	}
	
	public Map<String,Number> getMetrics() {
		return graph.getMetrics();
	}
	
}
