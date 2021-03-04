//import java.util.LinkedList;
import java.util.PriorityQueue;


public class Algorithm {
	private int nCities;
	//private int nRoads;
	//private int capital;
	private int[][] roadLength;
	private boolean[] selected;
	private City[] cities; //contem os lengths
	private int[] via;
	private PriorityQueue<City> q;
	//private LinkedList<Integer> linked = new LinkedList<Integer>();
	
	public Algorithm(int nCities, int nRoads, int capital, int[][] roadLength) {
		this.nCities = nCities;
		this.roadLength = roadLength;
		//dijkstra initialization
		selected = new boolean[nCities];
		cities = new City[nCities];
		via = new int[nCities];
		Compare c = new Compare();
		q = new PriorityQueue<City>(nCities, c);
		for(int i = 0; i < nCities; i++){
			if( i != capital){
				selected[i] = false;
				cities[i] = new City(i);
				via[i] = 0;
 			}
		}
		selected[capital] = false;
		//linked.add(capital);
		via[capital] = capital;
		cities[capital] = new City(capital, 0);
		q.add(cities[capital]);
		
	}
	
	public City[] run(){
		while(!q.isEmpty()){
			City c = q.remove();
			selected[c.getId()] = true;
			exploreCity(c);
		}
		return cities;
	}
	
	public void exploreCity(City c){
		for(int i = 0; i < nCities; i++){
			if(roadLength[c.getId()][i] > 0){
				if(!selected[i]){
					int newLength = c.getMinDistance() + roadLength[c.getId()][i];
					if (newLength < cities[i].getMinDistance()){
						boolean nodeIsInQueue = (cities[i].getMinDistance() < Integer.MAX_VALUE);
						via[i] = c.getId();
						if(nodeIsInQueue){
							q.remove(cities[i]);
							cities[i].setMinDistance(newLength);
							q.add(cities[i]);
						} else {
							cities[i].setMinDistance(newLength);
							q.add(cities[i]);
						}
					}
				}
				
			}
		}
	}

}
