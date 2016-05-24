package rover_kae;

import java.util.Deque;
import java.util.List;

import common.Coord;

public class Node {

	Coord coord;
	Node parentNode;
	// List<Node> adjacents;
	int f = -1, g = -1, h = -1; // movement cost

	public Node() {
	}

	// center is the parent of all adj
	public Node(Coord pos, Node parent, int fIn) {
		coord = pos;
		parentNode = parent;
		f = fIn;
	}
	
	public Node(Coord pos, Node parent) {
		coord = pos;
		parentNode = parent;
	}

	public void setF(int f) {
		f = f;
	}

	public void setParent(Node p){
		parentNode = p;
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
		return other.coord.equals(this.coord);
	}

	@Override
	public String toString() {
		return "Node [coord=" + coord + ", parent=" + parentNode.coord
				+ ", cost=" + f + "]";
	}

	public String str() {
		return "[Node: coord=" + coord.xpos + "," + coord.ypos + "\th=" + h
				+ ", g=" + g + ", f=" + f + "]";
	}
}