package searchalgorithm;

import java.util.*;

import searchproblem.*;

public class BreadthFirstOptimized implements SearchAlgorithm {
	
	private boolean done = false;
	private SearchProblem problem;
	private Node goal;
	private Queue<Node> frontier;
	private Set<Node> explored;
	private int expansions;
	private int generated;
	private int repeated;
	private long time;
	
	public BreadthFirstOptimized(SearchProblem p) {
		this.problem = p;
		frontier = new LinkedList<Node>();
		explored = new HashSet<Node>();
	}

	public Node searchSolution() {
		if( !done ) {
			long startTime = System.nanoTime();		
			goal = search();
			time = System.nanoTime() - startTime;
			done = true;
			problem = null;
		}
		return goal;
	}
	
	private Node search() {
		
		frontier.clear();
		Node initial = new Node(problem.getInitial());
		frontier.add(initial);
		generated++;
		if( problem.goalTest(initial.getState())) {
			return initial;
		}
		
		for(;;) {
			if( frontier.isEmpty() ) {
				return null;
			}
			Node n = frontier.remove();
			explored.add(n);
			for (Arc a : n.getState().successorFunction()){
				generated++;
				Node child = new Node(n,a);
				if(!explored.contains(child) && !frontier.contains(child)){
					if( problem.goalTest(child.getState())) {
						return child;
					}
					frontier.add(child);
				}
				else repeated++;
			}
			expansions++;
		}
	}
	
	public Map<String,Number> getMetrics() {
		Map<String,Number> metrics = new LinkedHashMap<String,Number>();
		
		metrics.put("Node Expansions",expansions);
		metrics.put("Nodes Generated",generated);
		metrics.put("State repetitions",repeated);		
		metrics.put("Runtime (s)", time/1E9);
		return metrics;
	}

}
