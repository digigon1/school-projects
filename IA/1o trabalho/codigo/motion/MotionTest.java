package motion;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import javax.swing.*;
import java.awt.Color;

import searchalgorithm.*;
import searchproblem.*;

public class MotionTest extends Component {

	static final String TERRAIN_PATH = "/Users/carlosdamasio/Documents/workspace/ia/src/motion/th.png";
	static final int SURFACE_STEP=10;
	static final boolean SURFACE_LEVELS=false;
	static final boolean ANIMATE=false;
	
    static BufferedImage img;
    static int startx, starty, goalx,goaly;
    
    public void paint(Graphics g) {
        g.drawImage(img, 0, 0, null);
        g.setColor( Color.WHITE);
        g.drawOval(startx-5, starty-5, 10, 10);
        g.drawOval(goalx-5, goaly-5, 10, 10);
        g.drawOval(goalx-3, goaly-3, 6, 6); 
        
    }

    public Dimension getPreferredSize() {
        if (img == null) {
             return new Dimension(100,100);
        } else {
           return new Dimension(img.getWidth(null), img.getHeight(null));
       }
    }

 	public static void main(String[] args) {		

		System.gc();
		BitmapTerrain t = new BitmapTerrain(TERRAIN_PATH);
		img = t.getTerrain();

		// AUXILIARY CLASS TO SHOW THE EVOLUTION OF THE FRONTIER
	    if( ANIMATE ) {
	        	AnimatedSearch.init(TERRAIN_PATH);
	    }

	    // DRAWS SURFACE LEVELS
		if( SURFACE_LEVELS ) {
			for( int x=0; x < t.getHorizontalSize() ; x++)
				for( int y=0 ; y < t.getVerticalSize() ; y++ )
					if( t.getHeight(x, y) % SURFACE_STEP < 1)
						img.setRGB(x, y, Color.PINK.getRGB() ^ img.getRGB(x, y));
		}
		
		// Creates and shows the window with the terrain
		JFrame f = new JFrame("Rover's path in Mars!");
        f.addWindowListener(new WindowAdapter(){
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
	
        f.add(new MotionTest());
        f.pack();
        f.setVisible(true);
		f.paint(f.getGraphics());

		// Some data points for testing the algorithms
		int pointx[] = {611, 495, 818, 772, 996, 805, 113, 698, 562, 415, 527, 28, 887, 955, 772, 
				927, 712, 434, 611, 721, 75, 966, 346, 872, 679, 623, 173, 264, 476, 458};
		
		int pointy[] = {190, 659, 10, 638, 741, 115, 714, 453, 707, 777, 124, 617, 880, 271, 13, 
				866, 615, 418, 836, 156, 132, 132, 64, 236, 766, 305, 540, 677, 506, 342};
		
		int i=0;
		int j=6;
		int startx = pointx[i]; int starty = pointy[i];
		int goalx = pointx[j];  int goaly = pointy[j];

        // Avoids side-effects of writing in the image
		t = new BitmapTerrain(TERRAIN_PATH);

		RoverState init = new RoverState(startx,starty,t);
		RoverState goal = new RoverState(goalx,goaly,t);

		// Solves the problem using AStarSearch
		InformedSearchProblem prob = new RoverProblem(init,goal);
		SearchAlgorithm u = new AStarSearch(prob);

		// Determines the solution and writes the metrics
		Node n = u.searchSolution();
		System.out.println(u.getMetrics());
		
		// Draws the solution in the screen
		if( n != null) {
			System.out.print( ((int) (n.getPathCost()*100)) + " ");
			System.out.println(n.getPath());
			RoverState current = (RoverState) init.clone();
			img.setRGB(current.getCoordX(), current.getCoordY(), 0xFF << 16);
			for( Object action: n.getPath()) {
				current.applyOperator(action);
				img.setRGB(current.getCoordX(), current.getCoordY(), 0xFF << 16 );
			}
		}
		f.paint(f.getGraphics());
		System.gc();
 	}

}
