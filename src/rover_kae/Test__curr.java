package rover_kae;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.DebugGraphics;

import org.junit.Test;

import supportTools.SwarmMapInit;
import common.Coord;
import common.PlanetMap;
import common.RoverLocations;
import common.ScienceLocations;
import controlServer.TestSwarmServer;

public class Test__curr {

	// @Test
	public void testRunDiffMaps() throws Exception {
		TestSwarmServer ss = new TestSwarmServer();
		Rv_curr rv = new Rv_curr();

		// ss.runServer("rv12_test.txt");
		ss.runServer("simple_rv12_test.txt");
	}

	public String pickADir(Coord from, Coord to) {

		int dx = to.xpos - from.xpos;
		int dy = to.ypos - from.ypos;

		if (dx * dx > dy * dy) {
			if (dx > 0) {
				return "E";
			} else {
				return "W";
			}
		} else {
			if (dy > 0) {
				return "S";
			} else {
				return "N";
			}
		}
	}

	// @Test
	public void testPickadir() throws Exception {
		Coord from = new Coord(5, 1);
		Coord to = new Coord(-1, 0);
		System.out.println(pickADir(from, to));
	}

	// @Test
	public void testNegation() {
		boolean A = true, B = true;
		System.out.println("!A & !B" + (!A && !B));
		System.out.println("!(A || B)" + (!A && !B));
	}

	// @Test
	public void testOutwardSearch0() throws Exception {
		int searchSize = 3;
		String[] directions = { "E", "S", "W", "N" };
		Coord curr = new Coord(4, 3), topL, bottomR;

		int n = 1; // length of sides
		int k = (n - 1) / 2, x, y, nn, xx, yy;

		Set<Coord> set = new HashSet<Coord>();

		for (int i = 1; i <= searchSize; i++) {
			topL = new Coord(curr.xpos - i, curr.ypos - i);
			bottomR = new Coord(curr.xpos + i, curr.ypos + i);
			// north edge
			x = topL.xpos;
			y = topL.ypos;
			nn = i + 2 * i;
			for (xx = x; xx <= bottomR.xpos; xx++) {
				set.add(new Coord(xx, y));
				System.out.print(xx + "n" + y + " ");
			}
			System.out.println();
			// east edge
			x = bottomR.xpos;
			y = topL.ypos + 1;
			for (yy = y; yy <= bottomR.ypos; yy++) {
				set.add(new Coord(x, yy));
				System.out.print(x + "e" + yy + " ");
			}
			System.out.println();
			// south edge
			x = bottomR.xpos - 1;
			y = bottomR.ypos;
			for (xx = x; xx >= topL.xpos; xx--) {
				set.add(new Coord(xx, y));
				System.out.print(xx + "s" + y + " ");
			}
			System.out.println();
			// west edge
			x = topL.xpos;
			y = bottomR.ypos - 1;
			for (yy = y; yy > topL.ypos; yy--) {
				set.add(new Coord(x, yy));
				System.out.print(x + "w" + yy + " ");
			}
			System.out.println();
		}

		topL = new Coord(curr.xpos - searchSize, curr.ypos - searchSize);
		bottomR = new Coord(curr.xpos + searchSize, curr.ypos + searchSize);

		for (int j = topL.ypos; j <= bottomR.ypos; j++) {
			for (int i = topL.xpos; i <= bottomR.xpos; i++) {

				if (set.contains(new Coord(i, j))) {
					System.out.print("(" + i + "," + j + ")");
				} else {
					System.out.print("  ");
				}
			}
			System.out.println();
		}

	}

	// Rv_curr rv = new Rv_curr();

	// ss.runServer("rv12_test.txt");

	// @Test
	public void testGetG() throws Exception {
		// TestSwarmServer ss = new TestSwarmServer();

		Node n = null;
		Node n00 = new Node(new Coord(0, 0), n, 4); // nw
		Node n01 = new Node(new Coord(0, 1), n, 4); // w
		Node n02 = new Node(new Coord(0, 2), n, 4); // sw
		Node n03 = new Node(new Coord(0, 3), n, 4);
		Node n04 = new Node(new Coord(0, 4), null, 4);

		Node n10 = new Node(new Coord(1, 0), n, 4); // w
		Node n11 = new Node(new Coord(1, 1), n, 4); // n
		Node n12 = new Node(new Coord(1, 2), n, 4); // s
		Node n13 = new Node(new Coord(1, 3), n, 4);
		Node n14 = new Node(new Coord(1, 4), n, 4);

		Node n20 = new Node(new Coord(2, 0), n, 4); // sw
		Node n21 = new Node(new Coord(2, 1), n, 4); // e
		Node n22 = new Node(new Coord(2, 2), n, 4); // se
		Node n23 = new Node(new Coord(2, 3), n, 4);
		Node n24 = new Node(new Coord(2, 4), n, 4);

		Node n30 = new Node(new Coord(3, 0), n, 4);
		Node n31 = new Node(new Coord(3, 1), n, 4);
		Node n32 = new Node(new Coord(3, 2), n, 4);
		Node n33 = new Node(new Coord(3, 3), n, 4);
		Node n34 = new Node(new Coord(3, 4), n, 4);

		Node n40 = new Node(new Coord(4, 0), n, 4);
		Node n41 = new Node(new Coord(4, 1), n, 4);
		Node n42 = new Node(new Coord(4, 2), n, 4);
		Node n43 = new Node(new Coord(4, 3), n, 4);
		Node n44 = new Node(new Coord(4, 4), n, 4);

		n00.setParent(n11);
		n10.setParent(n11);
		n20.setParent(n11);
		n01.setParent(n11);
		n21.setParent(n11);
		n12.setParent(n11);
		n02.setParent(n11);
		n12.setParent(n11);
		n22.setParent(n11);

		Node goal = n33, start = n11, center = n11, focus = n12;

		InABeeLine b = new InABeeLine();
		b.setF(focus, center, start);
		b.setG(center, focus);

		System.out.println(start.g);
		System.out.println(focus.g);

		// Rv_curr rv = new Rv_curr();

		// ss.runServer("rv12_test.txt");

	}

	// @Test
	public void testGetH() throws Exception {
		// TestSwarmServer ss = new TestSwarmServer();

		Node n = null;
		Node n00 = new Node(new Coord(0, 0), n, 4); // nw
		Node n01 = new Node(new Coord(0, 1), n, 4); // w
		Node n02 = new Node(new Coord(0, 2), n, 4); // sw
		Node n03 = new Node(new Coord(0, 3), n, 4);
		Node n04 = new Node(new Coord(0, 4), null, 4);

		Node n10 = new Node(new Coord(1, 0), n, 4); // w
		Node n11 = new Node(new Coord(1, 1), n, 4); // n
		Node n12 = new Node(new Coord(1, 2), n, 4); // s
		Node n13 = new Node(new Coord(1, 3), n, 4);
		Node n14 = new Node(new Coord(1, 4), n, 4);

		Node n20 = new Node(new Coord(2, 0), n, 4); // sw
		Node n21 = new Node(new Coord(2, 1), n, 4); // e
		Node n22 = new Node(new Coord(2, 2), n, 4); // se
		Node n23 = new Node(new Coord(2, 3), n, 4);
		Node n24 = new Node(new Coord(2, 4), n, 4);

		Node n30 = new Node(new Coord(3, 0), n, 4);
		Node n31 = new Node(new Coord(3, 1), n, 4);
		Node n32 = new Node(new Coord(3, 2), n, 4);
		Node n33 = new Node(new Coord(3, 3), n, 4);
		Node n34 = new Node(new Coord(3, 4), n, 4);

		Node n40 = new Node(new Coord(4, 0), n, 4);
		Node n41 = new Node(new Coord(4, 1), n, 4);
		Node n42 = new Node(new Coord(4, 2), n, 4);
		Node n43 = new Node(new Coord(4, 3), n, 4);
		Node n44 = new Node(new Coord(4, 4), n, 4);

		n00.setParent(n11);
		n10.setParent(n11);
		n20.setParent(n11);
		n01.setParent(n11);
		n21.setParent(n11);
		n12.setParent(n11);
		n02.setParent(n11);
		n12.setParent(n11);
		n22.setParent(n11);

		Node goal = n33, start = n11, focus = n04;
		InABeeLine b = new InABeeLine();
		b.setH(focus, goal);
		System.out.println(focus.h);

		// Rv_curr rv = new Rv_curr();

		// ss.runServer("rv12_test.txt");

	}

	// @Test
	public void testGetF() throws Exception {
		// TestSwarmServer ss = new TestSwarmServer();

		Node n = null;
		Node n00 = new Node(new Coord(0, 0), n, 4); // nw
		Node n01 = new Node(new Coord(0, 1), n, 4); // w
		Node n02 = new Node(new Coord(0, 2), n, 4); // sw
		Node n03 = new Node(new Coord(0, 3), n, 4);
		Node n04 = new Node(new Coord(0, 4), null, 4);

		Node n10 = new Node(new Coord(1, 0), n, 4); // w
		Node n11 = new Node(new Coord(1, 1), n, 4); // n
		Node n12 = new Node(new Coord(1, 2), n, 4); // s
		Node n13 = new Node(new Coord(1, 3), n, 4);
		Node n14 = new Node(new Coord(1, 4), n, 4);

		Node n20 = new Node(new Coord(2, 0), n, 4); // sw
		Node n21 = new Node(new Coord(2, 1), n, 4); // e
		Node n22 = new Node(new Coord(2, 2), n, 4); // se
		Node n23 = new Node(new Coord(2, 3), n, 4);
		Node n24 = new Node(new Coord(2, 4), n, 4);

		Node n30 = new Node(new Coord(3, 0), n, 4);
		Node n31 = new Node(new Coord(3, 1), n, 4);
		Node n32 = new Node(new Coord(3, 2), n, 4);
		Node n33 = new Node(new Coord(3, 3), n, 4);
		Node n34 = new Node(new Coord(3, 4), n, 4);

		Node n40 = new Node(new Coord(4, 0), n, 4);
		Node n41 = new Node(new Coord(4, 1), n, 4);
		Node n42 = new Node(new Coord(4, 2), n, 4);
		Node n43 = new Node(new Coord(4, 3), n, 4);
		Node n44 = new Node(new Coord(4, 4), n, 4);

		n00.setParent(n11);
		n10.setParent(n11);
		n20.setParent(n11);
		n01.setParent(n11);
		n21.setParent(n11);
		n12.setParent(n11);
		n02.setParent(n11);
		n12.setParent(n11);
		n22.setParent(n11);
		InABeeLine b = new InABeeLine();
		Node goal = n33, start = n11, center = n11;
		Node[] adjacents = { n00, n10, n20, n01, n21, n02, n12, n22 };
		Map<Coord, Node> computedNodes = new HashMap<Coord, Node>();

		// set center's cost
		b.setF(center, center, goal);

		// set all adjacents' cost
		for (Node node : adjacents) {
			Node focus = node;
			b.setF(focus, center, goal);
			System.out.println(focus.str());
			computedNodes.put(node.coord, node);
		}

		// print out computed adjacents
		Node nn;
		System.out.println("computedNodes: ");
		for (int j = 0; j < 3; j++) {
			for (int i = 0; i < 3; i++) {
				nn = computedNodes.get(new Coord(i, j));
				System.out.println(nn);
			}
		}
	} // ends testGetF()

	// @Test
	public void testMoveToNextCenter() throws Exception {
		// TestSwarmServer ss = new TestSwarmServer();

		Node n = null;
		Node n00 = new Node(new Coord(0, 0), n, 4); // nw
		Node n01 = new Node(new Coord(0, 1), n, 4); // w
		Node n02 = new Node(new Coord(0, 2), n, 4); // sw
		Node n03 = new Node(new Coord(0, 3), n, 4);
		Node n04 = new Node(new Coord(0, 4), null, 4);

		Node n10 = new Node(new Coord(1, 0), n, 4); // w
		Node n11 = new Node(new Coord(1, 1), n, 4); // n
		Node n12 = new Node(new Coord(1, 2), n, 4); // s
		Node n13 = new Node(new Coord(1, 3), n, 4);
		Node n14 = new Node(new Coord(1, 4), n, 4);

		Node n20 = new Node(new Coord(2, 0), n, 4); // sw
		Node n21 = new Node(new Coord(2, 1), n, 4); // e
		Node n22 = new Node(new Coord(2, 2), n, 4); // se
		Node n23 = new Node(new Coord(2, 3), n, 4);
		Node n24 = new Node(new Coord(2, 4), n, 4);

		Node n30 = new Node(new Coord(3, 0), n, 4);
		Node n31 = new Node(new Coord(3, 1), n, 4);
		Node n32 = new Node(new Coord(3, 2), n, 4);
		Node n33 = new Node(new Coord(3, 3), n, 4);
		Node n34 = new Node(new Coord(3, 4), n, 4);

		Node n40 = new Node(new Coord(4, 0), n, 4);
		Node n41 = new Node(new Coord(4, 1), n, 4);
		Node n42 = new Node(new Coord(4, 2), n, 4);
		Node n43 = new Node(new Coord(4, 3), n, 4);
		Node n44 = new Node(new Coord(4, 4), n, 4);

		n00.setParent(n11);
		n10.setParent(n11);
		n20.setParent(n11);
		n01.setParent(n11);
		n21.setParent(n11);
		n12.setParent(n11);
		n02.setParent(n11);
		n12.setParent(n11);
		n22.setParent(n11);
		InABeeLine b = new InABeeLine();
		Node goal = n33, start = n11, center = n11;
		Node[] adjacents = { n00, n10, n20, n01, n21, n02, n12, n22 };
		Map<Coord, Node> computedNodes = new HashMap<Coord, Node>();

		// set center's cost
		b.setF(center, center, goal);

		// set all adjacents' cost
		for (Node node : adjacents) {
			Node focus = node;
			b.setF(focus, center, goal);
			System.out.println(focus.str());
			computedNodes.put(node.coord, node);
		}

		// print out computed adjacents
		Node nn;
		System.out.println("computedNodes: ");
		for (int j = 0; j < 3; j++) {
			for (int i = 0; i < 3; i++) {
				nn = computedNodes.get(new Coord(i, j));
				System.out.println(nn);
			}
		}
	}

	// @Test
	public void testAddAdjToOpen() throws Exception {
		// TestSwarmServer ss = new TestSwarmServer();

		Node n = null;
		Node n00 = new Node(new Coord(0, 0), n, 4); // nw
		Node n01 = new Node(new Coord(0, 1), n, 4); // w
		Node n02 = new Node(new Coord(0, 2), n, 4); // sw
		Node n03 = new Node(new Coord(0, 3), n, 4);
		Node n04 = new Node(new Coord(0, 4), null, 4);

		Node n10 = new Node(new Coord(1, 0), n, 4); // w
		Node n11 = new Node(new Coord(1, 1), n, 4); // n
		Node n12 = new Node(new Coord(1, 2), n, 4); // s
		Node n13 = new Node(new Coord(1, 3), n, 4);
		Node n14 = new Node(new Coord(1, 4), n, 4);

		Node n20 = new Node(new Coord(2, 0), n, 4); // sw
		Node n21 = new Node(new Coord(2, 1), n, 4); // e
		Node n22 = new Node(new Coord(2, 2), n, 4); // se
		Node n23 = new Node(new Coord(2, 3), n, 4);
		Node n24 = new Node(new Coord(2, 4), n, 4);

		Node n30 = new Node(new Coord(3, 0), n, 4);
		Node n31 = new Node(new Coord(3, 1), n, 4);
		Node n32 = new Node(new Coord(3, 2), n, 4);
		Node n33 = new Node(new Coord(3, 3), n, 4);
		Node n34 = new Node(new Coord(3, 4), n, 4);

		Node n40 = new Node(new Coord(4, 0), n, 4);
		Node n41 = new Node(new Coord(4, 1), n, 4);
		Node n42 = new Node(new Coord(4, 2), n, 4);
		Node n43 = new Node(new Coord(4, 3), n, 4);
		Node n44 = new Node(new Coord(4, 4), n, 4);

		n00.setParent(n11);
		n10.setParent(n11);
		n20.setParent(n11);
		n01.setParent(n11);
		n21.setParent(n11);
		n12.setParent(n11);
		n02.setParent(n11);
		n12.setParent(n11);
		n22.setParent(n11);
		InABeeLine b = new InABeeLine();
		Node goal = n33, start = n11, center = n11;
		Node[] adjacents = { n00, n10, n20, n01, n21, n02, n12, n22 };
		Map<Coord, Node> nodesComputed = new HashMap<Coord, Node>();
		Deque<Coord> closed = new ArrayDeque<Coord>();
		closed.push(center.coord);

		b.computeAdjacents(center, start, goal, nodesComputed, closed);

		// print out computed adjacents
		Node nn;
		System.out.println("Done?: ");
		for (int j = 0; j < 3; j++) {
			for (int i = 0; i < 3; i++) {
				nn = nodesComputed.get(new Coord(i, j));
				System.out.println(nn);
			}
		}
	}

	@Test
	public void testShiftFocus() throws Exception {
		// TestSwarmServer ss = new TestSwarmServer();

		Node n = null;
		Node n00 = new Node(new Coord(0, 0), n, 4); // nw
		Node n01 = new Node(new Coord(0, 1), n, 4); // w
		Node n02 = new Node(new Coord(0, 2), n, 4); // sw
		Node n03 = new Node(new Coord(0, 3), n, 4);
		Node n04 = new Node(new Coord(0, 4), null, 4);

		Node n10 = new Node(new Coord(1, 0), n, 4); // w
		Node n11 = new Node(new Coord(1, 1), n, 4); // n
		Node n12 = new Node(new Coord(1, 2), n, 4); // s
		Node n13 = new Node(new Coord(1, 3), n, 4);
		Node n14 = new Node(new Coord(1, 4), n, 4);

		Node n20 = new Node(new Coord(2, 0), n, 4); // sw
		Node n21 = new Node(new Coord(2, 1), n, 4); // e
		Node n22 = new Node(new Coord(2, 2), n, 4); // se
		Node n23 = new Node(new Coord(2, 3), n, 4);
		Node n24 = new Node(new Coord(2, 4), n, 4);

		Node n30 = new Node(new Coord(3, 0), n, 4);
		Node n31 = new Node(new Coord(3, 1), n, 4);
		Node n32 = new Node(new Coord(3, 2), n, 4);
		Node n33 = new Node(new Coord(3, 3), n, 4);
		Node n34 = new Node(new Coord(3, 4), n, 4);

		Node n40 = new Node(new Coord(4, 0), n, 4);
		Node n41 = new Node(new Coord(4, 1), n, 4);
		Node n42 = new Node(new Coord(4, 2), n, 4);
		Node n43 = new Node(new Coord(4, 3), n, 4);
		Node n44 = new Node(new Coord(4, 4), n, 4);

		n00.setParent(n11);
		n10.setParent(n11);
		n20.setParent(n11);
		n01.setParent(n11);
		n21.setParent(n11);
		n12.setParent(n11);
		n02.setParent(n11);
		n12.setParent(n11);
		n22.setParent(n11);
		InABeeLine b = new InABeeLine();
		Node goal = n33, start = n11, center = n11;
		Node[] adjacents = { n00, n10, n20, n01, n21, n02, n12, n22 };
		Map<Coord, Node> nodesComputed = new HashMap<Coord, Node>();
		Deque<Coord> closed = new ArrayDeque<Coord>();
		closed.push(center.coord);

		Node nextCtr = b.computeAdjacents(center, start, goal, nodesComputed,
				closed);
		System.out.println("\tcheapest 1: " + nextCtr);
		closed.push(nextCtr.coord);
		System.out.println("\n\nafter first comp adj:");
		b.debugPrintAdjacents(nodesComputed);
		// nodesComputed.remove(nextCtr.coord);
		System.out.println();

		// ---- itr 2
		Node nextCtr2 = b.computeAdjacents(nextCtr, start, goal, nodesComputed,
				closed);
		System.out.println("\tcheapest 2: " + nextCtr2);
		closed.push(nextCtr2.coord);
		System.out.println("\n\nafter cecond comp adj:");
		b.debugPrintAdjacents(nodesComputed);
		// nodesComputed.remove(nextCtr2.coord);

		// ---- itr 3
		Node nextCtr3 = b.computeAdjacents(nextCtr2, start, goal, nodesComputed,
				closed);
		System.out.println("\tcheapest 3: " + nextCtr3);
		closed.push(nextCtr3.coord);
		// nodesComputed.remove(nextCtr3.coord);
		System.out.println("\n\nafter first comp adj:");
		b.debugPrintAdjacents(nodesComputed);

		// print out computed adjacents
		Node nn;
		System.out.println("Done?: ");
		for (int j = 0; j < 3; j++) {
			for (int i = 0; i < 3; i++) {
				nn = nodesComputed.get(new Coord(i, j));
				System.out.println(nn);
			}
		}
	}

}
