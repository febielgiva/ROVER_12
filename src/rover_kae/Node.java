package rover_kae;

import common.Coord;

public class Node {

	Coord currCoord;
	Node parentNode;

	public Node() {
	}

	public Node(Coord c, Node p) {
		currCoord = c;
		parentNode = p;
	}

	// cheaper distance computation (Manhattan heuristic cost)
	public int getH(Coord here, Coord target) {
		int dx = Math.abs(here.xpos - target.xpos);
		int dy = Math.abs(here.ypos - target.ypos);
		return dx + dy;
	}

	// more expensive distance computation (modified Pythagorean, movement cost)
	public int getG(Coord end) {
		int parentG = 0;
		if (parentNode.parentNode != null) {
			parentNode.getG(end);
		}
		int baseVal = 10, dist = 0;
		if (currCoord.xpos == end.xpos || currCoord.ypos == end.ypos) {
			// 14 because sqrt(10*10 + 10*10) = 1.414...
			baseVal = 14;
		}

		dist = parentG + baseVal;
		return dist;
	}

	// f is just g + h
	public int getF(Coord p1, Coord target) {
		return getH(p1, target) + getG(p1, target);
	}

}
