package motion;

import java.util.ArrayList;
import java.util.List;
import static java.lang.Math.*;

import searchproblem.*;


public class RoverState extends State {

    List<Arc> something;

    private int coordX;
    private int coordY;

    public RoverState(int x, int y, BitmapTerrain t) {
        coordX = x;
        coordY = y;
        something = new ArrayList<>();
    }

    @Override
    public List<Arc> successorFunction() {
        //TODO
        return null;
    }

    @Override
    public Arc successorState(Object op) {
        //TODO
        return null;
    }

    @Override
    public double applyOperator(Object op) {
        //TODO
        return 0.0;
    }

    @Override
    public Object clone() {
        //TODO
        return null;
    }

    @Override
    public int hashCode() {
        //TODO
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        //TODO
        return true;
    }

    public int getCoordX() {
        return coordX;
    }

    public int getCoordY() {
        return coordY;
    }
}
