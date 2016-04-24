package Kae;

import common.MapTile;

import enums.RoverName;
import enums.Science;
import enums.Terrain;

public class MapTileUtil extends MapTile {
	private Terrain terrain;
	private int elevation; // not currently used
	private Science science; // for use on ScanMap, not used on PlanetMap
	private boolean hasRover; // for use on ScanMap, not used on PlanetMap

	public MapTileUtil() {
		super();
	}

	public MapTileUtil(int notUsed) {
		// use any integer as an argument to create MapTile with no terrain
		super();
	}

	public MapTileUtil(String terrainLetter) {
		super();
	}

	public MapTileUtil(Terrain ter, int elev) {
		super();
	}

	public MapTileUtil(Terrain ter, Science sci, int elev, boolean hasR) {
		super();
	}
	

}
