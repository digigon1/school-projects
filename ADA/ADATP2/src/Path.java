enum Direction {UP, RIGHT, LEFT, DOWN}

public class Path {
    public int lion1x, lion1y;
    public int lion2x, lion2y;

    public int pathLength;
    public Direction lastDir;

    public Path(int lionX1, int lionY1, int lionX2, int lionY2){
        this(lionX1, lionY1, lionX2, lionY2, 0, null);
    }

    private Path(int lionX1, int lionY1, int lionX2, int lionY2, int size, Direction d){
        lion1x = lionX1;
        lion1y = lionY1;
        lion2x = lionX2;
        lion2y = lionY2;
        pathLength = size;
        lastDir = d;
    }

    public Path move(boolean move1, boolean move2, Direction d) {
        int dx = 0, dy = 0;

        switch (d) {
            case UP:
                dy = -1;
                break;
            case DOWN:
                dy = 1;
                break;
            case LEFT:
                dx = -1;
                break;
            case RIGHT:
                dx = 1;
                break;
        }

        return new Path(lion1x + (move1?dx:0), lion1y + (move1?dy:0), lion2x - (move2?dx:0), lion2y + (move2?dy:0), pathLength+1, d);
    }


    public boolean lionsInLove(int loveX, int loveY){
        return loveX == lion1x && loveY == lion1y && loveX == lion2x && loveY == lion2y;
    }


    @Override
    public boolean equals(Object o){
        if(o == null || !(o instanceof Path))
            return false;

        Path other = (Path) o;

        return lion1x == other.lion1x && lion1y == other.lion1y && lion2x == other.lion2x && lion2y == other.lion2y;
    }

    @Override
    public final int hashCode(){
        return lion1x + lion1y * 41 + lion2x * 1681 + lion2y * 68921;
    }
}


