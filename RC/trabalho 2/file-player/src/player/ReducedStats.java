package player;

import java.io.PrintStream;
import java.util.List;


// version of class Statistics for students

public class ReducedStats {
	
	int[] resolutionsCounts; 	// número de segmentos descarregados em cada resolução
	List<Integer> bitRates;
	
//	PrintStream logFile = System.out;
	PrintStream logFile = null;
	PrintStream results = System.out;
	
	int totalFilmSegments = 0;
	int totalPlayedSegments = 0;
	int totalBytes = 0;
	int rebuffering = 0;
	long start = 0;
	private int nSegments;
	private int totalBitrate = 0;

	public ReducedStats (List<Integer> rates, int nSegments) {
		this.bitRates = rates;
		resolutionsCounts = new int[rates.size()];
		this.nSegments = nSegments;
	}
	
	public void log (String message) {
		if ( logFile == null ) return;
		logFile.println("player stats: "+message);
	}
	
	public void result (String message) {
		results.println("player stats: "+message);
	}
	
	public void logGetSegment (int resolution, int bytes) {
		if ( totalFilmSegments == 0 ) start = System.currentTimeMillis();
		totalFilmSegments ++;
		totalBitrate += bitRates.get(resolution);
		totalBytes += bytes;
		resolutionsCounts[resolution] ++;
	}
	
	public void logPlaySegment () {
		totalPlayedSegments++;
	}
	
	
	public void logRebuffering () {
		rebuffering ++;
	}
	
	public void dumpStats() {
		result("got segments during: "+(System.currentTimeMillis()-start)/1000+" s");
		result("received "+totalFilmSegments+" segments w/ "+totalBytes/1024);
		result("rebuffered: "+rebuffering+" times");
		int total = 0;
		for (int i=0; i<resolutionsCounts.length; i++) {
			total += resolutionsCounts[i];
		}
		for (int i=0; i<resolutionsCounts.length; i++) {
			if (resolutionsCounts[i] != 0 )
			result("resolution: "+bitRates.get(i)+" received "+resolutionsCounts[i]+" segments "
					+(resolutionsCounts[i]*100/total)+" %");
		}
		result("Total number of segments: "+nSegments);
		result("Average bitrate: "+totalBitrate/totalFilmSegments);
	}

}
