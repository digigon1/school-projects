package motion;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class BitmapTerrain implements Terrain {

	private BufferedImage terrain;

	public BitmapTerrain(String filename) {
		
        try {
    		terrain = ImageIO.read(new File(filename));
        } catch (IOException e) {
        	throw new RuntimeException("Unable to read terrain from file " + filename);
        }
		
	}
	
	public int getHorizontalSize() {
		return terrain.getWidth(null);
	}
	
	public int getVerticalSize() {
		return terrain.getHeight(null);		
	}

	public int getHeight(int x, int y) {
		return 255-terrain.getRGB(x, y) & 0x0000FF;
	}
	
	public TerrainType getTerrainType(int x, int y) {
		int rgb, r, g;
		rgb = terrain.getRGB(x, y);
		g = ( rgb & 0x000100 ) >> 8;
		r = ( rgb & 0x010000 ) >> 16;
		if( r == 1)
			return TerrainType.SAND;
		else if ( g == 1)
			return TerrainType.PLAIN;
		else
			return TerrainType.ROCK;
	}

	public BufferedImage getTerrain() {
		return terrain;
	}
	
}
