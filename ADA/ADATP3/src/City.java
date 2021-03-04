public class City{
	private int id;
	private int minDistance;
	
	public City(int id) {
		this.setId(id);
		minDistance = Integer.MAX_VALUE;
	}
	
	public City(int id, int minDistance) {
		this.setId(id);
		this.minDistance = minDistance;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getMinDistance() {
		return minDistance;
	}

	public void setMinDistance(int minDistance) {
		this.minDistance = minDistance;
	}
}
