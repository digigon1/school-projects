import java.util.Comparator;

public class Compare implements Comparator<City>{

	@Override
	public int compare(City o1, City o2) {
		if (o1.getMinDistance() > o2.getMinDistance())
			return 1;
		else if (o1.getMinDistance() < o2.getMinDistance()){
			return -1;
		} else {
			return 0;
		}
	}
	
}
