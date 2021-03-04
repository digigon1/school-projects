package searchalgorithm;

import java.util.*;

import searchproblem.InformedSearchProblem;

public class UniformCostInformedSearch implements SearchAlgorithm{

	private GraphSearch graph;
	
	public UniformCostInformedSearch(InformedSearchProblem p) {
		Queue<Node> pqueue = new PriorityQueue<Node>(11, new Comparator<Node>() {	
			public int compare(Node o1, Node o2) {
			if( o1.getPathCost()+p.heuristic(o1)> o2.getPathCost()+p.heuristic(o2))
				return 1;
			else if ( o1.getPathCost() +p.heuristic(o1) < o2.getPathCost()+p.heuristic(o2))
				return -1;
			else
				return 0;
			}});
		graph = new GraphSearch(p,pqueue);
	}
	
	public Node searchSolution() {
		return graph.searchSolution();
	}
	
	public Map<String,Number> getMetrics() {
		return graph.getMetrics();
	}

}
