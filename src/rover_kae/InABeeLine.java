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
import common.MapTile;
import enums.Terrain;

public class InABeeLine {
	// get the shortest path based on A* algorithm
	public String shortestPath;

	public String getShortestPath(Coord start, Coord goal,
			Map<Coord, MapTile> mapTileLog) {

		StringBuffer sb = new StringBuffer();
		Map<Coord, Node> open = new HashMap<Coord, Node>();
		Deque<Coord> closed = new ArrayDeque<>();
		Map<Coord, Node> nodeComputed = new HashMap<Coord, Node>();
		Coord cheapest;
		Node s = new Node(start, null);
		Node g = new Node(goal, null);
		Node center;
		s.setF(computeF(s, s, g));

		// add start tile to closed
		center = s.clone();
		if (!open.containsKey(start)) {
			open.put(start, center);
		}
		closed.push(start);

		int itrTracker = 0;
		// until there are no more viable tiles
		while (!center.coord.equals(goal) && !open.isEmpty()) {
			itrTracker++;
			cheapest = computeAdjacents(center, s, g, nodeComputed, open,
					closed, mapTileLog);
			center = open.get(cheapest);
			// debug p out
			System.out.println("this itr[" + itrTracker + "]:\ncoord "
					+ center.coord + "\ncheapest of open "
					+ nodeComputed.get(cheapest) + "\nsize of open "
					+ open.size() + "\ncurr center " + center.str());

			closed.push(cheapest);
			open.remove(center);

		}
		
		for (Coord coord : closed) {
			System.out.println(coord);
		}
		return shortestPath;
	}

	private boolean hasAllTileInfo(int tlX, int tlY, int brX, int brY,
			Map<Coord, MapTile> mapTileLog) {

		for (int j = tlY; j < brY; j++) {
			for (int i = tlX; i < brX; i++) {
				if (!mapTileLog.containsKey(new Coord(i, j))) {
					return false;
				}
			}
		}
		return true;
	}

	// get the least expensive adjacent
	public Coord computeAdjacents(Node center, Node start, Node goal,
			Map<Coord, Node> nodesComputed, Map<Coord, Node> open,
			Deque<Coord> closed, Map<Coord, MapTile> mapTileLog) {

		open.remove(center.coord);
		System.out.println("inside computeAdjacents()\tcenter:" + center.str());

		List<Node> adjacents = new ArrayList<Node>();
		int x = center.coord.xpos;
		int y = center.coord.ypos;

		Coord n = new Coord(x, y - 1);
		examineThisAdjacent(center, goal, nodesComputed, open, closed,
				mapTileLog, adjacents, n);

		Coord ne = new Coord(x + 1, y - 1);
		examineThisAdjacent(center, goal, nodesComputed, open, closed,
				mapTileLog, adjacents, ne);

		Coord e = new Coord(x + 1, y);
		examineThisAdjacent(center, goal, nodesComputed, open, closed,
				mapTileLog, adjacents, e);

		Coord se = new Coord(x + 1, y + 1);
		examineThisAdjacent(center, goal, nodesComputed, open, closed,
				mapTileLog, adjacents, se);

		Coord s = new Coord(x, y + 1);
		examineThisAdjacent(center, goal, nodesComputed, open, closed,
				mapTileLog, adjacents, s);

		Coord sw = new Coord(x - 1, y + 1);
		examineThisAdjacent(center, goal, nodesComputed, open, closed,
				mapTileLog, adjacents, sw);

		Coord w = new Coord(x - 1, y);
		examineThisAdjacent(center, goal, nodesComputed, open, closed,
				mapTileLog, adjacents, w);

		Coord nw = new Coord(x - 1, y - 1);
		examineThisAdjacent(center, goal, nodesComputed, open, closed,
				mapTileLog, adjacents, nw);

		// debug print out
		for (Node node : adjacents) {
			System.out.println("adj " + node);
		}
		// System.out.println("debug print inside in a bee line class :D ");
		// debugPrintAdjacents(nodesComputed);
		// System.out.println("\n\n\n\n");

		return min(open);
	}

	private void examineThisAdjacent(Node center, Node goal,
			Map<Coord, Node> nodesComputed, Map<Coord, Node> open,
			Deque<Coord> closed, Map<Coord, MapTile> mapTileLog,
			List<Node> adjacents, Coord adjacent) {

		int thisG = -1;
		Node thisAdj;
		// debug print out
		System.out.println("inside examineThisAdjacent(" + adjacent.xpos + ","
				+ adjacent.ypos + ")\tcenter" + center.str());

		// if this node is not an obstacle or the one in the closed list
		if (!closed.contains(adjacent) && !isObsatacle(adjacent, mapTileLog)) {

			// if this node has been examined already w/ a different center
			if (nodesComputed.containsKey(adjacent)) {

				// is the stored g greater than newly computed g?
				thisAdj = nodesComputed.get(adjacent);
				thisG = computeG(center, thisAdj);
				if (thisG < thisAdj.g) {
					// if so,update the parent and g
					thisAdj.setParent(center);
					thisAdj.setG(thisG);
				}
			} else {
				Node node = new Node(adjacent, center, -1);
				node.setF(computeF(node, center, goal));
				System.out.println("this adj: " + node.str());

				adjacents.add(node);
				nodesComputed.put(node.coord, node);
				if (!open.containsKey(node.coord)) {
					open.put(node.coord, node);
				}
			}
		}
	}

	public void debugPrintAdjacents(Map<Coord, Node> adj) {
		for (Node node : adj.values()) {
			System.out.println(node);
		}
	}

	// start -> goal distance
	public void setH(Node focus, Node goal) {

		int dx = Math.abs(focus.coord.xpos - goal.coord.xpos);
		int dy = Math.abs(focus.coord.ypos - goal.coord.ypos);

		focus.h = (dx + dy) * 10;
	}

	// start -> goal distance
	public int computeH(Coord focus, Coord goal) {

		System.out.println("here: " + focus + "\t there: " + goal);
		int dx = Math.abs(focus.xpos - goal.xpos);
		int dy = Math.abs(focus.ypos - goal.ypos);

		return (dx + dy) * 10;
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
	}

	public int computeG(Node center, Node focus) {

		if (center.equals(focus)) {
			focus.g = 0;
			return 0;
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
		return g;
	}

	public int computeF(Node focus, Node center, Node goal) {

		setH(focus, goal);
		setG(center, focus);

		return (focus.h + focus.g);
	}

	private Coord min(Map<Coord, Node> open) {
		Coord min = new Coord(-1, -1);
		int minVal = Integer.MAX_VALUE, thisVal = 0;

		for (Node node : open.values()) {
			thisVal = node.f;
			if (minVal > thisVal) {
				min = node.coord;
				minVal = thisVal;
			}
		}

		return min;
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

	public boolean isObsatacle(Coord focus, Map<Coord, MapTile> mapTileLog) {
		MapTile tile = mapTileLog.get(focus);
		if (tile.getHasRover() || tile.getTerrain() == Terrain.ROCK
				|| tile.getTerrain() == Terrain.NONE
				|| tile.getTerrain() == Terrain.FLUID
				|| tile.getTerrain() == Terrain.SAND) {
			return true;
		} else {
			return false;
		}
	}

	public Coord[] getSearchArea(Coord p1, Coord p2, int k) {

		// given 2 points, find top-left-corner and bottom-right-corner
		int tlX = (p1.xpos < p2.xpos) ? p1.xpos : p2.xpos;
		int tlY = (p1.ypos < p2.ypos) ? p1.ypos : p2.ypos;
		int brX = (p1.xpos > p2.xpos) ? p1.xpos : p2.xpos;
		int brY = (p1.ypos > p2.ypos) ? p1.ypos : p2.ypos;

		// decrement top-left by k, increment bottom-right by k
		tlX -= k;
		tlY -= k;
		brX -= k;
		brY -= k;

		Coord[] corners = { new Coord(tlX, tlY), new Coord(brX, brY) };
		return corners;
	}
}
