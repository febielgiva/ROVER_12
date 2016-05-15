package supportTools;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import common.Coord;
import common.MapTile;
import common.ScanMap;
import enums.Terrain;

public class RoverMotionUtil {
	Random rd = new Random();

	// returns which direction to go in order to backtrack a step
	private String getBackTrackDirection(boolean[] cardinals, Coord prevCoord,
			Coord currCoord) {

		int currX = currCoord.xpos;
		int currY = currCoord.ypos;
		if (isPastPositonIsNorth(cardinals, prevCoord, currX, currY)) {
			return "N";
		} else if (isPastPositonIsEast(cardinals, prevCoord, currX, currY)) {
			return "E";
		} else if (isPastPositonIsSouth(cardinals, prevCoord, currX, currY)) {
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

	public boolean isTowardsWestIsObsatacle(MapTile[][] scanMapTiles,
			int centerIndex, int i) {
		if (scanMapTiles[centerIndex - i][centerIndex].getHasRover()
				|| scanMapTiles[centerIndex - i][centerIndex].getTerrain() == Terrain.ROCK
				|| scanMapTiles[centerIndex - i][centerIndex].getTerrain() == Terrain.NONE
				|| scanMapTiles[centerIndex - i][centerIndex].getTerrain() == Terrain.FLUID
				|| scanMapTiles[centerIndex - i][centerIndex].getTerrain() == Terrain.SAND) {
			return true;
		}
		return false;
	}

	public boolean isTowardsNorthIsObsatacle(MapTile[][] scanMapTiles,
			int centerIndex, int i) {
		if (scanMapTiles[centerIndex][centerIndex - i].getHasRover()
				|| scanMapTiles[centerIndex][centerIndex - i].getTerrain() == Terrain.ROCK
				|| scanMapTiles[centerIndex][centerIndex - i].getTerrain() == Terrain.NONE
				|| scanMapTiles[centerIndex][centerIndex - i].getTerrain() == Terrain.FLUID
				|| scanMapTiles[centerIndex][centerIndex - i].getTerrain() == Terrain.SAND) {
			return true;
		}
		return false;
	}

	public boolean isTowardsSouthIsObsatacle(MapTile[][] scanMapTiles,
			int centerIndex, int i) {
		if (scanMapTiles[centerIndex][centerIndex + i].getHasRover()
				|| scanMapTiles[centerIndex][centerIndex + i].getTerrain() == Terrain.ROCK
				|| scanMapTiles[centerIndex][centerIndex + i].getTerrain() == Terrain.NONE
				|| scanMapTiles[centerIndex][centerIndex + i].getTerrain() == Terrain.FLUID
				|| scanMapTiles[centerIndex][centerIndex + i].getTerrain() == Terrain.SAND) {
			return true;
		}
		return false;
	}

	public boolean isTowardsEastIsObsatacle(MapTile[][] scanMapTiles,
			int centerIndex, int i) {
		if (scanMapTiles[centerIndex + i][centerIndex].getHasRover()
				|| scanMapTiles[centerIndex + i][centerIndex].getTerrain() == Terrain.ROCK
				|| scanMapTiles[centerIndex + i][centerIndex].getTerrain() == Terrain.NONE
				|| scanMapTiles[centerIndex + i][centerIndex].getTerrain() == Terrain.FLUID
				|| scanMapTiles[centerIndex + i][centerIndex].getTerrain() == Terrain.SAND) {
			return true;
		}

		return false;
	}

	public double getDistanceBtw2Points(Coord p1, Coord p2) {

		int dx = p2.xpos - p1.xpos;
		int dy = p2.ypos - p1.ypos;

		return Math.sqrt((dx * dx) + (dy * dy));
	}

	public Coord getRover12TargetArea(Map<Coord, MapTile> mapTileLog,
			Coord targetLoc) {

		boolean isTargetLocReached = !mapTileLog.containsKey(targetLoc);
		int searchSize = 30, nullCounter = 0;

		if (!isTargetLocReached) {
			return targetLoc;
		}

		// whil()
		// tempCoord = new Coord(randomNum(0, targetLocation.getXpos()),
		// randomNum(0, targetLocation.getYpos()));
		// int nullCounter = 0;
		// // randomly pick a coordinate from the green corp's common storage,
		// and
		// // count # null cell in 33 x 33
		//
		// for (int j = tempCoord.getYpos() - searchSize / 2; j < tempCoord
		// .getYpos() + searchSize; j++) {
		// for (int i = tempCoord.getXpos() - searchSize / 2; i < tempCoord
		// .getYpos() + searchSize; i++) {
		// if (!mapTileLog.containsKey(tempCoord)) {
		// nullCounter++;
		// }
		// }
		// }
		return null;
	}

	public int randomNum(int min, int max) {
		return rd.nextInt(max + 1) + min;
	}

	// KS - must complete
	public boolean isObstacle(String direction, ScanMap scanMap) {

		int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
		int x = centerIndex, y = centerIndex, scanRange = 2;

		for (int i = 1; i < scanRange; i++) {
			if (direction == "S")
				x = centerIndex + i;
			else if (direction == "N")
				x = centerIndex - i;
			else if (direction == "E")
				y = centerIndex + i;
			else
				y = centerIndex - i;

			// Checks whether there is sand or rock in the next tile
			if (scanMap.getScanMap()[x][y].getTerrain() == Terrain.SAND
					|| scanMap.getScanMap()[x][y].getTerrain() == Terrain.ROCK) {
				return true;
			}
			return false;
		}
		return false;
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
