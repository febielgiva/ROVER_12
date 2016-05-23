package rover_kae;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import common.Coord;

public class InABeeLine {
	// get the shortest path based on A* algorithm
	public String shortestPath;

	public String getShortestPath(Coord start, Coord goal) {

		StringBuffer sb = new StringBuffer();
		// List<Coord> open, closed, adjacent;
		Map<Coord, Node> open = new HashMap<Coord, Node>();
		Deque<Coord> closed = new ArrayDeque<>(); // the shortest path
		Deque<Node> adjacent = new ArrayDeque<>();
		Map<Coord, Node> nodeComputed = new HashMap<Coord, Node>();

		int G = 0; // G value at start
		Node s = new Node(new Coord(start.xpos, start.ypos), null, getH(start,
				goal));
		int cost = getH(start, goal) + G;
		Node sParent = new Node(new Coord(-1, -1), null, -1), curr = new Node(
				start, sParent, cost);

		closed.offerFirst(curr.coord); // push

		// until curr becomes goal
		// while (!curr.coord.equals(goal)) {

		Node cheapest = computeAdjacent(curr, start, goal, nodeComputed);

		// }

		// debug print out
		System.out.println(cheapest);
		return shortestPath;
	}

	// get the least expensive adjacent
	private Node computeAdjacent(Node center, Coord start, Coord goal,
			Map<Coord, Node> nodesComputed) {

		List<Node> adjacents = new ArrayList<Node>();
		int x = center.coord.xpos;
		int y = center.coord.ypos;
		Node goalNode = new Node(goal, null, getH(start, goal));
		int cost = -1;

		Coord n = new Coord(x, y - 1);
		if (!nodesComputed.containsKey(n)) {

			Node N = new Node(n, center, -1);
			cost = setF(N, center, goalNode);
			N.setF(cost);
			System.out.println("N: " + N);
			adjacents.add(N);

		} else {
			adjacents.add(nodesComputed.get(n));
		}

		Coord ne = new Coord(x + 1, y - 1);
		if (!nodesComputed.containsKey(ne)) {

			Node NE = new Node(ne, center, -1);
			cost = setF(NE, center, goalNode);
			NE.setF(cost);
			adjacents.add(NE);

		} else {
			adjacents.add(nodesComputed.get(ne));
		}
		Coord e = new Coord(x + 1, y);
		if (!nodesComputed.containsKey(e)) {

			Node E = new Node(e, center, -1);
			cost = setF(E, center, goalNode);
			E.setF(cost);
			adjacents.add(E);
		} else {
			adjacents.add(nodesComputed.get(e));
		}
		Coord se = new Coord(x + 1, y + 1);
		if (!nodesComputed.containsKey(se)) {

			Node SE = new Node(se, center, -1);
			cost = setF(SE, center, goalNode);
			SE.setF(cost);
			adjacents.add(SE);

		} else {
			adjacents.add(nodesComputed.get(se));
		}
		Coord s = new Coord(x, y + 1);
		if (!nodesComputed.containsKey(s)) {

			Node S = new Node(s, center, -1);
			cost = setF(S, center, goalNode);
			S.setF(cost);
			adjacents.add(S);

		} else {
			adjacents.add(nodesComputed.get(s));
		}
		Coord sw = new Coord(x - 1, y + 1);
		if (!nodesComputed.containsKey(sw)) {

			Node SW = new Node(sw, center, -1);
			cost = setF(SW, center, goalNode);
			SW.setF(cost);
			adjacents.add(SW);

		} else {
			adjacents.add(nodesComputed.get(sw));
		}
		Coord w = new Coord(x - 1, y);
		if (!nodesComputed.containsKey(w)) {

			Node W = new Node(w, center, -1);
			cost = setF(W, center, goalNode);
			W.setF(cost);
			adjacents.add(W);

		} else {
			adjacents.add(nodesComputed.get(w));
		}
		Coord nw = new Coord(x - 1, y - 1);
		if (!nodesComputed.containsKey(nw)) {

			Node NW = new Node(nw, center, -1);
			cost = setF(NW, center, goalNode);
			NW.setF(cost);
			adjacents.add(NW);

		} else {
			adjacents.add(nodesComputed.get(nw));
		}

		// store all computed adjacents values (w costs) to nodesComputed before
		// returning
		for (Node node : adjacents) {
			nodesComputed.put(node.coord, node);
			// debug print out
			// System.out.println(node);
		}

		// printCells(nodesComputed);

		return min(adjacents);
	}

	// start -> goal distance
	public void setH(Node focus, Node goal) {

		System.out.println("here: " + focus.coord + "\t there: " + goal.coord);
		int dx = Math.abs(focus.coord.xpos - goal.coord.xpos);
		int dy = Math.abs(focus.coord.ypos - goal.coord.ypos);

		focus.h = dx + dy;
	}

	// start -> goal distance
	public int getH(Coord focus, Coord goal) {

		System.out.println("here: " + focus + "\t there: " + goal);
		int dx = Math.abs(focus.xpos - goal.xpos);
		int dy = Math.abs(focus.ypos - goal.ypos);

		return dx + dy;
	}

	// start -> focus (movement cost)
	public void setG(Node center, Node focus) {

		if (center.equals(focus)) {
			focus.g = 0;
			return;
		}

		int cx = center.coord.xpos;
		int cy = center.coord.ypos;
		int fx = focus.coord.xpos;
		int fy = focus.coord.ypos;

		// diagonal cells: 14 because sqrt(10*10 + 10*10) = 1.414...
		int baseVal = 14;
		// verical or horizontalcell cells
		if (cx == fx || cy == fy) {
			baseVal = 10;
		}

		int g = center.g + baseVal;
		focus.g = g;

		// debug print out
		// System.out.println("p1 " + center.coord + "\tp2 " + focus);
		// System.out.println("p1.g=" + center.cost + "\tp2.g=" + g);
	}

	public int setF(Node focus, Node center, Node goal) {

		// set h if not done yet
		if (focus.h < 0) {
			setH(focus, goal);
		}

		// set g if not done yet
		if (focus.g < 0) {
			setG(center, focus);
		}

		return focus.h + focus.g;
	}

	private Node min(List<Node> adjacents) {
		int minIdx = 0, minVal = Integer.MAX_VALUE, thisVal = 0;
		for (int i = 0; i < adjacents.size(); i++) {
			thisVal = adjacents.get(i).f;
			if (minVal > thisVal) {
				minIdx = i;
				minVal = thisVal;
			}
		}
		return adjacents.get(minIdx);
	}

	// expensive distance computation (Pythagorean)
	private double getDistanceBtw2Points(Coord p1, Coord p2) {

		int dx = p2.xpos - p1.xpos;
		int dy = p2.ypos - p1.ypos;

		return Math.sqrt((dx * dx) + (dy * dy));
	}

	// args must be adjacent two points
	private String getDir(Coord p1, Coord p2) {

		int dx = p2.xpos - p1.xpos;
		int dy = p2.ypos - p1.ypos;

		if (dx > 0) {
			return "E";
		}
		if (dx < 0) {
			return "W";
		}
		if (dy > 0) {
			return "S";
		} else {
			// dy < 0
			return "N";
		}
	}

	public void printCells(Map<Coord, Node> nodeComputed) {
		Node node;
		for (int j = 0; j < 2; j++) {
			for (int i = 0; i < 2; i++) {
				node = nodeComputed.get(new Coord(i, j));
				if (node != null) {
					System.out.print(node.str());
				}
			}
			System.out.println();
		}
	}
}
