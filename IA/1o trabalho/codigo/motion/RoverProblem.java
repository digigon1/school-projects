package motion;

import static java.lang.Math.*;
import searchalgorithm.Node;
import searchproblem.*;

public class RoverProblem extends InformedSearchProblem {


    public RoverProblem(RoverState init, RoverState goal) {
        super(init, goal);
    }

    @Override
    public double heuristic(Node n) {
        //TODO
        return 0.0;
    }
}
