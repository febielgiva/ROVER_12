package rover_kae;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import common.Coord;

public class Test_ks {

	@Test
	public void testFindMaxIndeces() {
		System.out.println(getDistanceBetween2Points(new Coord(1, 3),
				new Coord(4, 8)));
		int[] array = { 4, 6, 2, 9, 1, 17, 2, 17, 5 };
		Set<Integer> maxes = findMaxIndeces(array);
		for (Integer num : maxes) {
			System.out.print(num + " ");
		}

	}

	// @Test
	public void testGetDistanceBetween2Points() {
		System.out.println(getDistanceBetween2Points(new Coord(1, 3),
				new Coord(4, 8)));

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

	private double getDistanceBetween2Points(Coord p1, Coord p2) {
		// sqrt((x2-x1)^2+(y2-y1)^2)

		return Math.sqrt(Math.pow(p2.getXpos() - p1.getXpos(), 2)
				+ Math.pow(p2.getYpos() - p1.getYpos(), 2));
	}

}
