package searchalgorithm;

import java.util.*;
import searchproblem.*;

public class GraphSearch implements SearchAlgorithm {

	//private final HashSet<State> explored;
	private SearchProblem problem;
	private Queue<Node> frontier;
	private HashMap<Node, Node> explored;
	private Node goal;
	private int expansions, generated, repeated;
	private boolean done;

	public GraphSearch(SearchProblem problem, Queue<Node> q) {
		this.problem = problem;
		this.frontier = q;
		this.explored = new HashMap<Node, Node>();
		//this.explored = new HashSet<State>();
		this.expansions = 0;
		this.generated = 0;
		this.done = false;
		this.goal = null;
	}

	public Node searchSolution(){
		if(!done){
			goal = this.search();
			done = true;
			frontier = null;
			explored = null;
		}
		return goal;
	}
	
	public Node search() {
		//TODO
		Node node = new Node(problem.getInitial());
		frontier.add(node);
		generated++;
		for (;;){
			if(frontier.isEmpty())
				return null;

			node = frontier.remove();
			if(problem.goalTest(node.getState()))
				return node;

			if(!explored.containsKey(node)){
				explored.put(node, node);
				expansions++;
				List<Node> children = node.Expand();
				frontier.addAll(children);
				generated += children.size();
			} else {
				repeated++;
			}
		}
	}

	public Map<String,Number> getMetrics() {
		//TODO
		Map<String,Number> metrics = new LinkedHashMap<String,Number>();

		metrics.put("Node Expansions",expansions);
		metrics.put("Nodes Generated",generated);
		metrics.put("Nodes Repeated", repeated);
		return metrics;
	}
	
}
