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

public class WavePatternMotionUtil {
	Random rd = new Random();
	Map<Coord, MapTile> mapTileLog;
	List<Coord> unvisited;

	public WavePatternMotionUtil(Map<Coord, MapTile> mapTileLog) {
		this.mapTileLog = mapTileLog;
	}

	

	// returns which direction to go in order to backtrack a step
	private String getBackTrackDirection(boolean[] cardinals, Coord prevCoord,
			Coord currCoord) {

		int currX = currCoord.xpos;
		int currY = currCoord.ypos;
		int prevX = prevCoord.xpos;
		int prevY = prevCoord.ypos;
		
		if (prevY==currY+1 && prevX==currX) {
			return "N";
		} else if (prevY==currY && prevX==currX+1) {
			return "E";
		} else if (prevY-1==currY && prevX==currX) {
			return "S";
		} else {
			return "W";
		}
	}

	private boolean isPastPositonIsNorth(boolean[] cardinals, Coord eachCoord,
			int currentXPos, int currentYPos) {
		int previousXPos = eachCoord.xpos;
		int previousYPos = eachCoord.ypos;
		if ((previousXPos == currentXPos) && (previousYPos == currentYPos - 1)) {
			return true;
		}
		return false;
	}

	private boolean isPastPositonIsWest(boolean[] cardinals, Coord eachCoord,
			int currentXPos, int currentYPos) {
		int previousXPos = eachCoord.xpos;
		int previousYPos = eachCoord.ypos;
		if ((previousXPos == currentXPos - 1) && (previousYPos == currentYPos)) {
			return true;
		}
		return false;
	}

	private boolean isPastPositonIsSouth(boolean[] cardinals, Coord eachCoord,
			int currentXPos, int currentYPos) {
		int previousXPos = eachCoord.xpos;
		int previousYPos = eachCoord.ypos;
		if ((previousXPos == currentXPos) && (previousYPos == currentYPos + 1)) {
			return true;
		}
		return false;
	}

	public boolean isPastPositonIsEast(boolean[] cardinals, Coord eachCoord,
			int currentXPos, int currentYPos) {
		int previousXPos = eachCoord.xpos;
		int previousYPos = eachCoord.ypos;
		if ((previousXPos == currentXPos + 1) && (previousYPos == currentYPos)) {
			return true;
		}
		return false;
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

	public Coord getCurrTargetCoord(Coord globalTargetLoc) {

		boolean isTargetLocReached = !mapTileLog.containsKey(globalTargetLoc);
		int searchSize = 30, nullCounter = 0;

		if (!visited(globalTargetLoc)) {
			return globalTargetLoc;
		}
		
		if(unvisited.size()<1){
			
		}
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
