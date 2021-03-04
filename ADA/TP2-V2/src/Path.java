
public class Path {
    public int lion1x, lion1y;
    public int lion2x, lion2y;

    public int pathLength;

    public Path(int lionX1, int lionY1, int lionX2, int lionY2){
        this(lionX1, lionY1, lionX2, lionY2, 0);
    }

    private Path(int lionX1, int lionY1, int lionX2, int lionY2, int size){
        lion1x = lionX1;
        lion1y = lionY1;
        lion2x = lionX2;
        lion2y = lionY2;
        pathLength = size;
    }

    public Path move(Direction d) {
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

        return new Path(lion1x + dx, lion1y + dy, lion2x - dx, lion2y + dy, pathLength+1);
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
}


