package common;

public class ScanMap {
	protected MapTile[][] scanArray;
	protected int edgeSize;
	protected Coord centerPoint;

	public ScanMap() {
		this.scanArray = null;
		this.edgeSize = 0;
		this.centerPoint = null;
	}

	public ScanMap(MapTile[][] scanArray, int size, Coord centerPoint) {
		this.scanArray = scanArray;
		this.edgeSize = size;
		this.centerPoint = centerPoint;
	}

	public MapTile[][] getScanMap() {
		return scanArray;
	}

	public void debugPrintMap() {
		System.out.println("edge size: " + edgeSize);
		for (int k = 0; k < edgeSize + 2; k++) {
			System.out.print("--");
		}

		System.out.print("\n");
		for (int j = 0; j < edgeSize; j++) {
			System.out.print("| ");
			for (int i = 0; i < edgeSize; i++) {
				// check and print edge of map has first priority
				if (scanArray[j][i].getTerrain().toString().equals("NONE")) {
					System.out.print("XX");

					// next most important - print terrain and/or science
					// locations
					// terrain and science
				} else if (!(scanArray[j][i].getTerrain().toString()
						.equals("SOIL"))
						&& !(scanArray[i][j].getScience().toString()
								.equals("NONE"))) {
					// both terrain and science

					System.out.print(scanArray[j][i].getTerrain().toString()
							.substring(0, 1)
							+ scanArray[j][i].getScience().getSciString());
					// just terrain
				} else if (!(scanArray[j][i].getTerrain().toString()
						.equals("SOIL"))) {
					System.out.print(scanArray[j][i].getTerrain().toString()
							.substring(0, 1)
							+ " ");
					// just science
				} else if (!(scanArray[j][i].getScience().toString()
						.equals("NONE"))) {
					System.out.print(" "
							+ scanArray[j][i].getScience().getSciString());

					// if still empty check for rovers and print them
				} else if (scanArray[j][i].getHasRover()) {
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

	public int getEdgeSize() {
		return edgeSize;
	}
}
