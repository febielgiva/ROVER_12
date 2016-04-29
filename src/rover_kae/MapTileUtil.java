package rover_kae;

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
		super(ter, elev);
	}

	public MapTileUtil(Terrain ter, Science sci, int elev, boolean hasR) {
		super(ter, sci, elev, hasR);
	}

	public MapTileUtil clone(){
		return new MapTileUtil(terrain,science,elevation,hasRover);
	}
	
	public void debugPrintMapTileArray(MapTile[][] mapTileArray) {

		int edgeSize = mapTileArray.length;
		System.out.println("edge size: " + edgeSize);
		for (int k = 0; k < edgeSize + 2; k++) {
			System.out.print("--");
		}
		System.out.print("\n");
		for (int j = 0; j < edgeSize; j++) {
			System.out.print("| ");
			for (int i = 0; i < edgeSize; i++) {
				// check and print edge of map has first priority
				if (mapTileArray[i][j].getTerrain().toString().equals("NONE")) {
					System.out.print("XX");

					// next most important - print terrain and/or science
					// locations
					// terrain and science
				} else if (!(mapTileArray[i][j].getTerrain().toString()
						.equals("SOIL"))
						&& !(mapTileArray[i][j].getScience().toString()
								.equals("NONE"))) {
					// both terrain and science

					System.out.print(mapTileArray[i][j].getTerrain().toString()
							.substring(0, 1)
							+ mapTileArray[i][j].getScience().getSciString());
					// just terrain
				} else if (!(mapTileArray[i][j].getTerrain().toString()
						.equals("SOIL"))) {
					System.out.print(mapTileArray[i][j].getTerrain().toString()
							.substring(0, 1)
							+ " ");
					// just science
				} else if (!(mapTileArray[i][j].getScience().toString()
						.equals("NONE"))) {
					System.out.print(" "
							+ mapTileArray[i][j].getScience().getSciString());

					// if still empty check for rovers and print them
				} else if (mapTileArray[i][j].getHasRover()) {
					System.out.print("[]");

					// nothing here so print nothing
				} else {
					System.out.print("  ");
				}
			}
			System.out.print(" |\n");
		}
		for (int k = 0; k < edgeSize + 2; k++) {
			System.out.print("--");
		}
		System.out.print("\n");
	}

}
