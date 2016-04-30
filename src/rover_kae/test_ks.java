package rover_kae;

import static org.junit.Assert.*;

import org.junit.Test;

import common.Coord;
import common.MapTile;
import enums.Terrain;
import enums.Science;
import common.ScanMap;

public class test_ks {

	@Test
	public void testMapTile() {
		MapTile mt = new MapTile(0);

	}

	// @Test
	public void testBooleanDefault() {
		boolean[][] tf = new boolean[3][3];
		System.out.println(tf[0][2]);
	}

	public void printMapTileArray(MapTile[][] mta) {
		for (int i = 0; i < mta.length; i++) {
			for (int j = 0; j < mta[i].length; j++) {
				System.out.print(mta[i][j].getTerrain() + "("
						+ mta[i][j].getScience() + ")" + " ");
			}
			System.out.println();
		}
	}
}
