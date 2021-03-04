package searchalgorithm;

import java.util.*;

import searchproblem.Arc;
import searchproblem.InformedSearchProblem;
import searchproblem.SearchProblem;
import searchproblem.State;

public class AStarSearch implements SearchAlgorithm {

    private static final int INITIAL_QUEUE_SIZE = 11;

    InformedSearchProblem prob;
    Node node;
    Queue<Node> frontier;
    HashMap<Node, Node> explored;
    private int expansions, generated, repeated;


    public AStarSearch(InformedSearchProblem prob) {
        expansions = 0;
        repeated = 0;
        generated = 1;

        this.prob = prob;

        node = new Node(prob.getInitial());

        this.frontier = new PriorityQueue<>(INITIAL_QUEUE_SIZE,
                (Node o1, Node o2) ->
                        (o1.getPathCost()+prob.heuristic(o1) > o2.getPathCost()+prob.heuristic(o2))?
                                1
                                :(o1.getPathCost()+prob.heuristic(o1) < o2.getPathCost()+prob.heuristic(o2))?
                                        -1
                                        :0
        );
        frontier.add(node);

        explored = new HashMap<>();
    }

    @Override
    public Node searchSolution() {
        //TODO
        for(;;){
            if(frontier.isEmpty())
                return null;

            node = frontier.remove();

            if(prob.goalTest(node.getState()))
                return node;

            //TODO
            if(!explored.containsKey(node)){
                expansions++;
                explored.put(node, node);
                List<Node> l = node.Expand();
                generated += l.size();
                frontier.addAll(l);
            } else {
                repeated++;
                Node test = explored.get(node);
                if(test.getPathCost()+prob.heuristic(test) > node.getPathCost()+prob.heuristic(node)){
                    explored.put(node, node);
                    frontier.addAll(node.Expand());
                }
            }
        }
    }

    @Override
    public Map<String, Number> getMetrics() {
        //TODO
        Map<String, Number> r = new HashMap<>();
        r.put("expansions", expansions);
        r.put("generated", generated);
        r.put("repeated", repeated);
        return r;
    }
}
