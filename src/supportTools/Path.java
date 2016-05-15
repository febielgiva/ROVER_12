package supportTools;

import java.util.HashMap;
import java.util.Map;

import common.Coord;

public class Path {
	
	private Coord currCoord;
	private Coord prevCoord;
	private int numVisited;
	// Map<direction, # consecutive open steps> (e.g. <"E", 5>, <"S", 3>, etc)  
	private Map<String, Integer> openPaths = new HashMap<String, Integer>();
	

	
	
}
