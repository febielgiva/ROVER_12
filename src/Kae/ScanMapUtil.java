package Kae;

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
}
