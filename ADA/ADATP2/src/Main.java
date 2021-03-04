import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.*;


public class Main {
    //DEBUG
    private static final boolean DEBUG = true;
    private static final boolean SPEED_TEST = true;
    private static int ran, exp, gen;

	public static void main(String[] args) {
	    //DEBUG
		if(DEBUG)
            ran = 0;

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

                if(SPEED_TEST) {
                    long totalTime = 0;
                    for (int i = 0; i < 500; i++) {
                        long t = System.currentTimeMillis();
                        run(m, lion1x, lion1y, lion2x, lion2y, lovex, lovey);
                        totalTime += System.currentTimeMillis()-t;
                    }
                    System.out.println("Average time: "+(totalTime/500.));
                    return;
                } else {
                    run(m, lion1x, lion1y, lion2x, lion2y, lovex, lovey);
                }

                if(DEBUG)
                    ran++;
            }
		} catch (NoSuchElementException e){

        } catch (Exception e){
        	/*
        	StringWriter sw = new StringWriter();
        	PrintWriter pw = new PrintWriter(sw);
        	e.printStackTrace(pw);
        	String m = sw.toString();
        	*/

        	if(true)//m.contains("Exception"))
        		System.exit(1);
        	else
        		for(;;);
		}

		//for(;;);
	}

    private static void run(Map m, int lion1x, int lion1y, int lion2x, int lion2y, int lovex, int lovey) {
	    long t;
	    if(DEBUG) {
            gen = 0;
            t = System.currentTimeMillis();
        }

        Path p = new Path(lion1x, lion1y, lion2x, lion2y);
        if(p.lionsInLove(lovex, lovey)) {
            System.out.println(0);
            return;
        }

        LinkedList<Path> q = new LinkedList<>();
        q.add(p);
        Set<Path> explored = new HashSet<>();
        explored.add(p);

        //DEBUG
        if(DEBUG)
            exp = 0;

        while (!q.isEmpty()) {
            if(DEBUG)
                exp++;

            p = q.remove();

            Direction old = p.lastDir;
            lion1x = p.lion1x; lion2x = p.lion2x;
            lion1y = p.lion1y; lion2y = p.lion2y;

            boolean move1, move2;
            move1 = m.canMove(lion1x-1, lion1y);
            move2 = m.canMove(lion2x+1, lion2y);
            if(old != Direction.RIGHT  && (move1||move2)) {
                if (addPath(p, Direction.LEFT, move1, move2, explored, q, lovex, lovey)) {
                    System.out.println(q.remove().pathLength);
                    if(DEBUG)
                        System.out.println(System.currentTimeMillis()-t+" "+exp+" "+gen);
                    return;
                }
            }

            move1 = m.canMove(lion1x, lion1y-1);
            move2 = m.canMove(lion2x, lion2y-1);
            if(old != Direction.DOWN && (move1||move2)) {
                if (addPath(p, Direction.UP, move1, move2, explored, q, lovex, lovey)) {
                    System.out.println(q.remove().pathLength);
                    if(DEBUG)
                        System.out.println(System.currentTimeMillis()-t+" "+exp+" "+gen);
                    return;
                }
            }

            move1 = m.canMove(lion1x+1, lion1y);
            move2 = m.canMove(lion2x-1, lion2y);
            if(old != Direction.LEFT && (move1||move2)) {
                if (addPath(p, Direction.RIGHT, move1, move2, explored, q, lovex, lovey)) {
                    System.out.println(q.remove().pathLength);
                    if(DEBUG)
                        System.out.println(System.currentTimeMillis()-t+" "+exp+" "+gen);
                    return;
                }
            }

            move1 = m.canMove(lion1x, lion1y+1);
            move2 = m.canMove(lion2x, lion2y+1);
            if(old != Direction.UP && (move1||move2)) {
                if (addPath(p, Direction.DOWN, move1, move2, explored, q, lovex, lovey)) {
                    System.out.println(q.remove().pathLength);
                    if(DEBUG)
                        System.out.println(System.currentTimeMillis()-t+" "+exp+" "+gen);
                    return;
                }
            }
        }

        System.out.println("NO LOVE");


    }

    private static boolean addPath(Path p, Direction d, boolean move1, boolean move2, Set<Path> explored, LinkedList<Path> q, int loveX, int loveY) {
        Path newPath = p.move(move1, move2, d);

        //DEBUG
        if(DEBUG)
            gen++;

        if (p != newPath && !explored.contains(newPath)) {
            if(newPath.lionsInLove(loveX, loveY)) {
                q.addFirst(newPath);
                return true;
            } else {
                explored.add(newPath);
                q.add(newPath);
            }
        }
        return false;
    }
}
