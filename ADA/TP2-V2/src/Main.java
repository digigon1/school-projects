import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

public class Main {
	public static void main(String[] args) {
		int rows;
		int cols;
		int lovex;
		int lovey;
		int lion1x;
		int lion1y;
		int lion2x;
		int lion2y;
		char[][] map;

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		try{
			String l;
			while((l = in.readLine()) != null) {

				StringTokenizer tkn = new StringTokenizer(l);
				rows = Integer.parseInt(tkn.nextToken());
				cols = Integer.parseInt(tkn.nextToken());
				map = new char[rows][cols];

				tkn = new StringTokenizer(in.readLine());
				lovey = Integer.parseInt(tkn.nextToken()) - 1;
				lovex = Integer.parseInt(tkn.nextToken()) - 1;
				lion1y = Integer.parseInt(tkn.nextToken()) - 1;
				lion1x = Integer.parseInt(tkn.nextToken()) - 1;
				lion2y = Integer.parseInt(tkn.nextToken()) - 1;
				lion2x = Integer.parseInt(tkn.nextToken()) - 1;

				for (int i = 0; i < rows; i++) {
					String str = in.readLine();
					for (int j = 0; j < cols; j++) {
						map[i][j] = str.charAt(j);
					}
				}

				Map m = new Map(map);

				run(m, new boolean[rows][cols][rows][cols], lion1x, lion1y, lion2x, lion2y, lovex, lovey);
			}
		} catch (Exception e){
        	e.printStackTrace();
		}
	}

	private static void run(Map m, boolean[][][][] explored, int lion1x, int lion1y, int lion2x, int lion2y, int lovex, int lovey){
		Path p = new Path(lion1x, lion1y, lion2x, lion2y);
		if(p.lionsInLove(lovex, lovey)) {
			System.out.println(0);
			return;
		}
		explored[p.lion1y][p.lion1x][p.lion2y][p.lion2x] = true;
		explored[p.lion2y][p.lion2x][p.lion1y][p.lion1x] = true;

		LinkedList<Path> q = new LinkedList<>();
		q.add(p);

		while (!q.isEmpty()) {

			p = q.remove();

			for(Direction d : Direction.values()){
				Path newPath = m.move(d, p);

				if (!p.equals(newPath) && !explored[newPath.lion1y][newPath.lion1x][newPath.lion2y][newPath.lion2x]) {
					explored[newPath.lion1y][newPath.lion1x][newPath.lion2y][newPath.lion2x] = true;
					explored[newPath.lion2y][newPath.lion2x][newPath.lion1y][newPath.lion1x] = true;
					if(newPath.lionsInLove(lovex, lovey)) {
						System.out.println(newPath.pathLength);
						return;
					}
					q.add(newPath);
				}
			}

		}

		System.out.println("NO LOVE");
	}
}
