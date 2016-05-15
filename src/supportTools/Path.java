package supportTools;

import java.util.HashMap;
import java.util.Map;

import common.Coord;

public class Path {
	
	private static long idSeed = 0;
	private long id=0;
	private Coord currCoord;
	private Coord prevCoord;
	private int numVisited;
	// Map<direction, # consecutive open steps> (e.g. <"E", 5>, <"S", 3>, etc)  
	private Map<String, Integer> openPaths = new HashMap<String, Integer>();
	
	
	// returns "N","E","S", or "W" to go back a step
	public String getBackTrackDir(){
		int currX = currCoord.xpos;
		int currY
		int n = cur,e,s,w
		
	}
	
}
