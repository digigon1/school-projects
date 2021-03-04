package searchalgorithm;

import java.util.*;

import motion.AnimatedSearch;
import motion.RoverState;
//import motion.AnimatedSearch;
//import motion.RoverState;
import searchproblem.InformedSearchProblem;

public class AStarSearch implements SearchAlgorithm{
	
	private boolean done = false;
	private InformedSearchProblem problem;
	private Queue<Node> frontier;
	private Map<Node,Node> explored;
	private Node goal;
	private long time;
	private int expansions;
	private int generated;
	private int repeated;
	
	public AStarSearch(InformedSearchProblem p) {
		problem=p;
		frontier=new PriorityQueue<Node>(11, new Comparator<Node>() {	
			public int compare(Node o1, Node o2) {
			if( o1.getPathCost()+p.heuristic(o1)> o2.getPathCost()+p.heuristic(o2))
				return 1;
			else if ( o1.getPathCost() +p.heuristic(o1) < o2.getPathCost()+p.heuristic(o2))
				return -1;
			else
				return 0;
			}});
		
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
		frontier.clear();
		Node initial = new Node(problem.getInitial());
		frontier.add(initial);
		generated++;
		for(;;){
			if( frontier.isEmpty() ) {
				return null;
			}
			Node n = frontier.remove();
			AnimatedSearch.draw(((RoverState) n.getState()).getCoordX(),((RoverState)n.getState()).getCoordY());
			if( problem.goalTest(n.getState())) {
				return n;
			}
			
			if(!explored.containsKey(n)){
				explored.put(n, n);
				List<Node> children = n.Expand();
				expansions++;
				generated+=children.size();
				frontier.addAll(children);
			}
			
			else{
				repeated++;
				Node node = explored.get(n);
				if(node.getPathCost()+problem.heuristic(node)>n.getPathCost()+problem.heuristic(n)){
					explored.put(n,n);
					frontier.addAll(n.Expand());
				}
			}
		}
	}

	public Map<String, Number> getMetrics() {
		Map<String,Number> metrics = new LinkedHashMap<String,Number>();
		metrics.put("Node Expansions",expansions);
		metrics.put("Nodes Generated",generated);
		metrics.put("Nodes Repeated",repeated);
		metrics.put("Runtime (s)", time/1E9);
		return metrics;
	}
}
