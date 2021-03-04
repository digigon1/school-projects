package searchalgorithm;

import java.util.*;

//import motion.AnimatedSearch;
//import motion.RoverState;
import searchproblem.*;

public class GraphSearch implements SearchAlgorithm {
	
	private boolean done = false;
	private SearchProblem problem;
	private Queue<Node> frontier;
	private Map<Node,Node> explored;
	private Node goal;
	private long time;
	private int expansions;
	private int generated;
	private int repeated;

	public GraphSearch(SearchProblem p, Queue<Node> q) {
		problem=p;
		frontier=q;
		explored = new HashMap<Node,Node>();
		goal = null;
		expansions = 0;
		generated = 0;
		repeated = 0;
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
		frontier.add(new Node(problem.getInitial()));
		generated++;
		
		for(;;){
			if( frontier.isEmpty() )
				return null;
			
			Node node = frontier.remove();
			//AnimatedSearch.draw(((RoverState) node.getState()).getCoordX(),((RoverState)node.getState()).getCoordY());

			if( problem.goalTest(node.getState())) {
				return node;
			}
			
			if(!explored.containsKey(node)){
				explored.put(node,node);
				List<Node> children = node.Expand();
				expansions++;
				generated += children.size();
				frontier.addAll(children);
			}
			
			else repeated++;
		
		}
	}

	public Map<String,Number> getMetrics() {
		Map<String,Number> metrics = new LinkedHashMap<String,Number>();
		metrics.put("Node Expansions",expansions);
		metrics.put("Nodes Generated",generated);
		metrics.put("Nodes Repeated",repeated);
		metrics.put("Runtime (s)", time/1E9);
		return metrics;
	}
	
}
