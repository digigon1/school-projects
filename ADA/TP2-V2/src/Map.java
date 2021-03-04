enum Direction {UP, RIGHT, LEFT, DOWN}

public class Map {

	private char[][] m;

	public Map(char[][] m){
		this.m = m;
	}

	public static boolean canMove(char[][] m, int x, int y){
		return x >= 0 && x < m[0].length && y >= 0 && y < m.length && m[y][x] == '.';
	}

	public Path move(Direction d, Path p) {
		Path newPath = p.move(d);

		if(!canMove(m, newPath.lion1x, newPath.lion1y)) {
			newPath.lion1x = p.lion1x;
			newPath.lion1y = p.lion1y;
		}

		if(!canMove(m, newPath.lion2x, newPath.lion2y)) {
			newPath.lion2x = p.lion2x;
			newPath.lion2y = p.lion2y;
		}

		return newPath;
	}
}
