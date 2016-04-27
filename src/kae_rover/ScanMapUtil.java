package kae_rover;

import java.util.Arrays;

import common.Coord;
import common.MapTile;
import common.ScanMap;
import enums.Science;
import enums.Terrain;

public class ScanMapUtil extends ScanMap {

	public ScanMapUtil() {
		super();
	}

	public ScanMapUtil(MapTile[][] scanArray, int size, Coord centerPoint) {
		super(scanArray, size, centerPoint);
	}

	public ScanMapUtil(MapTile[][] scanArray) {
		super(scanArray, scanArray.length, new Coord(scanArray.length,
				scanArray.length));
	}

	public MapTile[][] getScanMap() {
		return scanArray;
	}

	public boolean containsScience() {
		for (MapTile[] mapTiles : scanArray) {
			for (MapTile mapTile : mapTiles) {
				if (!mapTile.getScience().equals(Science.NONE)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean containsSand() {
		for (MapTile[] mapTiles : scanArray) {
			for (MapTile mapTile : mapTiles) {
				if (mapTile.getTerrain().equals(Terrain.SAND)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public MapTile[][] cloneMapTile(MapTile[][] original){
		
		MapTile[][] copy = new MapTile[original.length][original[0].length];
		Terrain ter; Science sci; int elev; boolean hasR;
		for (int i = 0; i < original.length; i++) {
			for (int j = 0; j < original[i].length; j++) {
				
				ter = original[i][j].getTerrain();
				sci = original[i][j].getScience();
				elev = original[i][j].getElevation();
				hasR = original[i][j].getHasRover();
				
				copy[i][j] = new MapTile(ter, sci, elev, hasR);
			}
		}		
		return copy;
	}
}
