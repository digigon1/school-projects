package motion;

public interface Terrain {

	public enum TerrainType {
		PLAIN, SAND, ROCK
	}

	public int getHorizontalSize();
	public int getVerticalSize();
	
	public int getHeight(int x, int y);
	public TerrainType getTerrainType(int x, int y);
	
}
