package supportTools;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import common.Coord;
import common.MapTile;
import common.ScanMap;
import enums.Terrain;

public class RoverMotionUtil {
	Random rd = new Random();
	Map<Coord, MapTile> mapTileLog;
	List<Coord> unvisited;

	
	public RoverMotionUtil(Map<Coord, MapTile> mapTileLog) {
		this.mapTileLog = mapTileLog;
	}

	public double getDistanceBtw2Points(Coord p1, Coord p2) {

		int dx = p2.xpos - p1.xpos;
		int dy = p2.ypos - p1.ypos;

		return Math.sqrt((dx * dx) + (dy * dy));
	}

	public boolean visited(Coord pos) {
		if (mapTileLog.containsKey(pos)) {
			return true;
		}
		return false;
	}

	// KS - impl
	public Coord getCurrTargetCoord(Coord globalTargetLoc) {
		return null;
	}

	public int randomNum(int min, int max) {
		return rd.nextInt(max + 1) + min;
	}

	private Set<Integer> findMaxIndeces(int[] array) {
		/*
		 * returns the index/indeces of the element(s) that hold(s) the maximum
		 * value
		 */
		int max = Integer.MIN_VALUE, maxIndex = -1;
		Set<Integer> tie = new HashSet<Integer>();
		for (int i = 0; i < array.length; i++) {
			if (max < array[i]) {
				maxIndex = i;
				max = array[i];
			}
		}
		tie.add(maxIndex);
		/*
		 * if 2 or more quadrant ties, return the farthest from current location
		 * of rover 12
		 */
		for (int i = 0; i < array.length; i++) {
			if (max == array[i]) {
				tie.add(i);
			}
		}
		return tie;
	}

}
