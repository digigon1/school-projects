package searchalgorithm;

import java.util.*;

import searchproblem.SearchProblem;

public class TreeSearch implements SearchAlgorithm {

	private boolean done = false;
	private SearchProblem problem;
	private Node goal;
	private Queue<Node> frontier;
	private int expansions;
	private int generated;
	
	public TreeSearch(SearchProblem p, Queue<Node> q) {
		problem = p;
		goal = null;
		frontier = q;
		expansions = 0;
		generated = 0;
	}
	
	public Node searchSolution() {
		if( !done ) {
			goal = search();
			done = true;
			frontier = null;
			problem = null;
		}
		return goal;
	}

	private Node search() {		
		frontier.clear();
		frontier.add(new Node(problem.getInitial()));
		generated++;
		for(;;) {
			if( frontier.isEmpty() ) {
				return null;
			}
			Node n = frontier.remove();
			if( problem.goalTest(n.getState())) {
				return n;
			}
			expansions++;
			List<Node> children = n.Expand();
			generated += children.size();
			frontier.addAll(children);
		}
	}
	
	public Map<String,Number> getMetrics() {
		Map<String,Number> metrics = new LinkedHashMap<String,Number>();
		
		metrics.put("Node Expansions",expansions);
		metrics.put("Nodes Generated",generated);
		return metrics;
	}
	
}
