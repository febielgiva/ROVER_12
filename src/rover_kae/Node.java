package rover_kae;

import java.util.Deque;
import java.util.List;

import common.Coord;

public class Node {

	Coord coord;
	Node parentNode;
	// List<Node> adjacents;
	int cost; // movement cost

	public Node() {
	}

	// center is parent of all adj
	public Node(Coord pos, Node parent, int cost) {
		coord = pos;
		parentNode = parent;
	}

	public void setF(int f) {
		cost = f;
	}

	// KS - ask someone about how to take advantage of this (comparison?)
	// @Override
	// public int hashCode() {
	// // return the coordinate's hashCode value
	// return coord.hashCode();
	// }
	@Override
	public boolean equals(Object o) {
		Node other = (Node) o;
		// return other.coord.equals(this.coord) &&
		// other.parentNode.coord.equals(this.parentNode.coord);
		return other.coord.equals(this.coord);
	}

	@Override
	public String toString() {
		return "Node [coord=" + coord + ", parent=" + parentNode.coord
				+ ", cost=" + cost + "]";
	}
}
