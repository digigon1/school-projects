import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.StringTokenizer;


public class Main {

	public static void main(String[] args) {
		int nCities;
		int nRoads;
		int[][] roadLength;
		int capital1;
		int capital2;

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		try{

			StringTokenizer tkn = new StringTokenizer(in.readLine());
			nCities= Integer.parseInt(tkn.nextToken());
			nRoads = Integer.parseInt(tkn.nextToken());
			roadLength = new int[nCities][nCities];
			for(int i = 0; i < nRoads; i++){
				tkn = new StringTokenizer(in.readLine());
				int city1 = Integer.parseInt(tkn.nextToken());
				int city2 = Integer.parseInt(tkn.nextToken());
				int length = Integer.parseInt(tkn.nextToken());
				roadLength[city1][city2] = length;
				roadLength[city2][city1] = length;
			}
			tkn = new StringTokenizer(in.readLine());
			capital1 = Integer.parseInt(tkn.nextToken());
			capital2 = Integer.parseInt(tkn.nextToken());
			
			Algorithm dijkstra1 = new Algorithm(nCities, nRoads, capital1, roadLength);
			Algorithm dijkstra2 = new Algorithm(nCities, nRoads, capital2, roadLength);
			
			City[] distancesCap1 = dijkstra1.run();
			City[] distancesCap2 = dijkstra2.run();
			
			int district1 = 0;
			int district2 = 0;
			int eitherdistrict = 0;
			
			for(int j = 0; j < nCities; j++){
				if(distancesCap1[j].getMinDistance() < distancesCap2[j].getMinDistance()){
					district1++;
				} else if (distancesCap1[j].getMinDistance() > distancesCap2[j].getMinDistance()){
					district2++;
				} else {
					eitherdistrict++;
				}
			}
			
			System.out.println(district1 + " " + district2 + " " + eitherdistrict);

		} catch (Exception e){
			System.err.println("EXCEPTION CAUGHT");
		}
	}
}
