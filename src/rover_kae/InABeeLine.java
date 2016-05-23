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
		int cost = getH(start, goal) + G;
		Node curr = new Node(start, null, cost);

		closed.offerFirst(curr.coord); // push

		// until curr becomes goal
		// while (!curr.coord.equals(goal)) {

		Node cheapest = computeAdjacent(curr, goal, nodeComputed);		

		// }
		
		// debug print out
		System.out.println(cheapest);
		return shortestPath;
	}

	// get the least expensive adjacent
	private Node computeAdjacent(Node center, Coord goal,
			Map<Coord, Node> nodesComputed) {

		List<Node> adjacents = new ArrayList<Node>();
		int x = center.coord.xpos;
		int y = center.coord.ypos;

		Coord n = new Coord(x, y - 1);
		if (!nodesComputed.containsKey(n)) {
			int costN = getF(center, n);
			Node N = new Node(n, center, costN);
			adjacents.add(N);
		} else {
			adjacents.add(nodesComputed.get(n));
		}

		Coord ne = new Coord(x + 1, y - 1);
		if (!nodesComputed.containsKey(ne)) {
			int costNE = getF(center, ne);
			Node NE = new Node(ne, center, costNE);
			adjacents.add(NE);
		} else {
			adjacents.add(nodesComputed.get(ne));
		}
		Coord e = new Coord(x + 1, y);
		if (!nodesComputed.containsKey(e)) {
			int costE = getF(center, e);
			Node E = new Node(e, center, costE);
			adjacents.add(E);
		} else {
			adjacents.add(nodesComputed.get(e));
		}
		Coord se = new Coord(x + 1, y + 1);
		if (!nodesComputed.containsKey(se)) {
			int costSE = getF(center, se);
			Node SE = new Node(se, center, costSE);
			adjacents.add(SE);
		} else {
			adjacents.add(nodesComputed.get(se));
		}
		Coord s = new Coord(x, y + 1);
		if (!nodesComputed.containsKey(s)) {
			int costS = getF(center, s);
			Node S = new Node(s, center, costS);
			adjacents.add(S);
		} else {
			adjacents.add(nodesComputed.get(s));
		}
		Coord sw = new Coord(x - 1, y + 1);
		if (!nodesComputed.containsKey(sw)) {
			int costSW = getF(center, sw);
			Node SW = new Node(sw, center, costSW);
			adjacents.add(SW);
		} else {
			adjacents.add(nodesComputed.get(sw));
		}
		Coord w = new Coord(x - 1, y);
		if (!nodesComputed.containsKey(w)) {
			int costW = getF(center, w);
			Node W = new Node(w, center, costW);
			adjacents.add(W);
		} else {
			adjacents.add(nodesComputed.get(w));
		}
		Coord nw = new Coord(x - 1, y - 1);
		if (!nodesComputed.containsKey(nw)) {
			int costNW = getF(center, nw);
			Node NW = new Node(nw, center, costNW);
			adjacents.add(NW);
		} else {
			adjacents.add(nodesComputed.get(nw));
		}

		// store all computed adjacents values (w costs) to nodesComputed before
		// returning
		for (Node node : adjacents) {
			nodesComputed.put(node.coord, node);
			// debug print out
			System.out.println(node);
		}

		return min(adjacents);
	}

	// start -> goal distance
	public int getH(Coord p1, Coord p2) {

		int dx = Math.abs(p1.xpos - p2.xpos);
		int dy = Math.abs(p1.ypos - p2.ypos);		
		return dx + dy;
	}

	// start -> focus (movement cost)
	public int getG(Node center, Coord focus) {

		int centerX = center.coord.xpos;
		int centerY = center.coord.ypos;

		int baseVal = 14; // 14 because sqrt(10*10 + 10*10) = 1.414...
		if (centerX == focus.xpos || centerY == focus.ypos) {
			baseVal = 10;
		}
		int g = center.cost + baseVal;
		
		//debug print out
		System.out.println("p1 "+center.coord+"\tp2 "+focus);
		System.out.println("p1.cost="+center.cost+"\tp2.cost="+g);
		
		

		return g;
	}

	// f(=cost) is just g + h (p1 = center, p2 = adj on focus)
	public int getF(Node p1, Coord p2) {
		return getH(p1.coord, p2) + getG(p1, p2);
	}

	private Node min(List<Node> adjacents) {
		int minIdx = 0, minVal = Integer.MAX_VALUE, thisVal = 0;
		for (int i = 0; i < adjacents.size(); i++) {
			thisVal = adjacents.get(i).cost;
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
}
