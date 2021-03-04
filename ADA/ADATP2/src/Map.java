
public class Map {
    private final char[][] m;

    public Map(char[][] m){
        this.m = m;
    }

    public boolean canMove(int x, int y){
        return x >= 0 && x < m[0].length && y >= 0 && y < m.length && m[y][x] == '.';
    }
}
