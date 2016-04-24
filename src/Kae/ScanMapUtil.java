package Kae;

import java.util.Arrays;

import common.Coord;
import common.MapTile;
import common.ScanMap;

public class ScanMapUtil extends ScanMap {
	private MapTile[][] scanArray;
	private int edgeSize;
	private Coord centerPoint;

	public ScanMapUtil() {
		super();
	}

	public ScanMapUtil(MapTile[][] scanArray, int size, Coord centerPoint) {
		super();
	}

	public MapTile[][] getScanMap() {
		return scanArray;
	}

	public boolean containsScience() {
		for (MapTile[] mapTiles : scanArray) {
			for (MapTile mapTile : mapTiles) {
				if (!mapTile.getScience().equals("NONE")) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean containsSand() {
		for (MapTile[] mapTiles : scanArray) {
			for (MapTile mapTile : mapTiles) {
				if (!mapTile.getTerrain().equals("NONE")) {
					return true;
				}
			}
		}
		return false;
	}
}
