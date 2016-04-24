package Kae;

import common.MapTile;

import enums.RoverName;
import enums.Science;
import enums.Terrain;

public class MapTileUtil extends MapTile {

	public MapTileUtil() {
		super();
	}

	public MapTileUtil(int notUsed) {
		// use any integer as an argument to create MapTile with no terrain
		super();
	}

	public MapTileUtil(String terrainLetter) {
		super(terrainLetter);
	}

	public MapTileUtil(Terrain ter, int elev) {
		super(ter,elev);
	}

	public MapTileUtil(Terrain ter, Science sci, int elev, boolean hasR) {
		super(ter,sci,elev,hasR);
	}
	

}
