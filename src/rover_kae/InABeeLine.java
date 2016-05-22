package rover_kae;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import common.Coord;

public class InABeeLine {
	// get the shortest path based on A* algorithm
	private String shortestPath;

	public String getShortestPath(Coord p1, Coord p2) {

		StringBuffer sb = new StringBuffer();
		// List<Coord> open, closed, adjacent;
		Deque<Coord> open = new ArrayDeque<>();
		Deque<Coord> closed = new ArrayDeque<>();
		Deque<Node> adjacent = new ArrayDeque<>(); // parents
		Node currNode = new Node(p1, null), parentNode = null;

		// while start != target
		while (!currNode.currCoord.equals(p2)) {

			// set previous current as the parent node
			// do adjacent calculation buz
			evalAdjacent(adjacent, parentNode, currNode.currCoord.xpos, currNode.currCoord.ypos);
			// out of all adj elements, the one with minF becomes next curr

		}
		return shortestPath;
	}

	private void evalAdjacent(Deque<Node> adjacent, Node p, int currX, int currY) {
		
		
		
		Node n = new Node(new Coord(currX, currY - 1), p);
		Node ne = new Node(new Coord(currX + 1, currY - 1), p);
		Node e = new Node(new Coord(currX + 1, currY), p);
		Node se = new Node(new Coord(currX + 1, currY + 1), p);
		Node s = new Node(new Coord(currX, currY + 1), p);
		Node sw = new Node(new Coord(currX - 1, currY + 1), p);
		Node w = new Node(new Coord(currX - 1, currY), p);
		Node nw = new Node(new Coord(currX - 1, currY - 1), p);
		
		ge
	}

	private Coord getMinFCoord(List<Node> adjacents, Coord c, Coord p) {
		int minIdx = 0, minVal = Integer.MAX_VALUE, thisVal = 0;
		for (int i = 0; i < adjacents.size(); i++) {
			thisVal = adjacents.get(i).getF(c, p);
			if (minVal > thisVal) {
				minIdx = i;
				minVal = thisVal;
			}
		}
		return adjacents.get(minIdx).currCoord;
	}

	// expensive distance computation (Pythagorean)
	private double getDistanceBtw2Points(Coord p1, Coord p2) {

		int dx = p2.xpos - p1.xpos;
		int dy = p2.ypos - p1.ypos;

		return Math.sqrt((dx * dx) + (dy * dy));
	}
}
