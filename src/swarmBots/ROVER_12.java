package swarmBots;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import rover_febi.NextMoveModel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import common.Coord;
import common.MapTile;
import common.ScanMap;
import enums.Science;
import enums.Terrain;

/**
 * The seed that this program is built on is a chat program example found here:
 * http://cs.lmu.edu/~ray/notes/javanetexamples/ Many thanks to the authors for
 * publishing their code examples
 * 
 * ROVER_12 Spec: Drive = wheels, Tool 1 = spectral sensor, Tool 2 = range
 * extender
 */

// this comment is for commit
public class ROVER_12 {

	protected BufferedReader in;
	protected PrintWriter out;
	protected String rovername, line;
	protected ScanMap scanMap;
	protected Coord targetLocation, rovergroupStartPosition, currentLoc,
			previousLoc;
	protected int sleepTime, serverMapSize;
	protected String SERVER_ADDRESS = "localhost";
	protected static final int PORT_ADDRESS = 9537;
	protected MapTile[][] mapTileLog = new MapTile[serverMapSize][serverMapSize];

	public ROVER_12() {
		// constructor
		System.out.println("ROVER_12 rover object constructed");
		rovername = "ROVER_12";
		SERVER_ADDRESS = "localhost";
		// this should be a safe but slow timer value
		sleepTime = 300; // in milliseconds - smaller is faster, but the server
							// will cut connection if it is too small
	}

	public ROVER_12(String serverAddress) {
		// constructor
		System.out.println("ROVER_12 rover object constructed");
		rovername = "ROVER_12";
		SERVER_ADDRESS = serverAddress;
		sleepTime = 200; // in milliseconds - smaller is faster, but the server
							// will cut connection if it is too small
	}

	/**
	 * Connects to the server then enters the processing loop.
	 */
	private void run() throws IOException, InterruptedException {

		// Make connection and initialize streams
		// TODO - need to close this socket
		Socket socket = new Socket(SERVER_ADDRESS, PORT_ADDRESS); // set port
		try { // here
			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);

			// Gson gson = new GsonBuilder().setPrettyPrinting().create();

			// Process all messages from server, wait until server requests
			// Rover ID
			// name
			while (true) {
				String line = in.readLine();
				if (line.startsWith("SUBMITNAME")) {
					out.println(rovername); // This sets the name of this
											// instance
											// of a swarmBot for identifying the
											// thread to the server
					break;
				}
			}

			// ********* Rover logic setup *********

			String line = "";
			Coord rovergroupStartPosition = null;
			Coord targetLocation = null;

			/**
			 * Get initial values that won't change
			 */
			// **** get equipment listing ****
			ArrayList<String> equipment = new ArrayList<String>();
			equipment = getEquipment();
			System.out.println(rovername + " equipment list results "
					+ equipment + "\n");

			// **** Request START_LOC Location from SwarmServer ****
			requestStartLoc();
			System.out.println(rovername + " START_LOC "
					+ rovergroupStartPosition);

			// **** Request TARGET_LOC Location from SwarmServer ****
			requestTargetLoc();
			System.out.println(rovername + " TARGET_LOC " + targetLocation);

			// could be velocity limit or obstruction etc.
			boolean blocked = false;

			String[] cardinals = new String[4];
			cardinals[0] = "N";
			cardinals[1] = "E";
			cardinals[2] = "S";
			cardinals[3] = "W";

			String currentDir = cardinals[0];
			Coord currentLoc = null;
			Coord previousLoc = null;

			/**
			 * #### Rover controller process loop ####
			 */
			while (true) {

				// **** Request Rover Location from SwarmServer ****
				setCurrentLoc();

				System.out.println(rovername + " currentLoc at start: "
						+ currentLoc);

				// after getting location set previous equal current to be able
				// to check for stuckness and blocked later
				previousLoc = currentLoc;
				System.out.println("curr: " + currentLoc);
				System.out.println("prev: " + previousLoc);

				// ***** do a SCAN ******

				// gets the scanMap from the server based on the Rover current
				// location
				doScan();

				// prints the scanMap to the Console output for debug purposes
				scanMap.debugPrintMap();

				// ***** MOVING *****
				// try moving east 5 block if blocked

				// pull the MapTile array out of the ScanMap object
				MapTile[][] scanMapTiles = scanMap.getScanMap();

				int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
				// tile S = y + 1; N = y - 1; E = x + 1; W = x - 1

				boolean stuck;
				roverMotionLogic(scanMapTiles, centerIndex);

				setCurrentLoc();

				// test for stuckness
				stuck = currentLoc.equals(previousLoc);

				// System.out.println("ROVER_12 stuck test " + stuck);
				System.out.println("ROVER_12 blocked test " + blocked);

				// this is the Rovers HeartBeat, it regulates how fast the Rover
				// cycles through the control loop
				Thread.sleep(sleepTime);

				System.out
						.println("ROVER_12 ------------ bottom process control --------------");

				// This catch block closes the open socket connection to the
				// server
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					System.out.println("ROVER_12 problem closing socket");
				}
			}
		}

	}

	// Thanks to Febi's awesome contribution!
	private void roverMotionLogic(MapTile[][] scanMapTiles, int centerIndex) {
		boolean goingSouth = false;
		boolean goingEast = true;
		boolean goingNorth = false;
		boolean goingWest = true;
		boolean stuck = false; // just means it did not change locations
								// between requests,

		// logic if going in east
		if (goingEast) {

			// if next move to east is an obstacle
			if (scanMapTiles[centerIndex + 1][centerIndex].getHasRover()
					|| scanMapTiles[centerIndex + 1][centerIndex].getTerrain() == Terrain.ROCK
					|| scanMapTiles[centerIndex + 1][centerIndex].getTerrain() == Terrain.NONE
					|| scanMapTiles[centerIndex + 1][centerIndex].getTerrain() == Terrain.FLUID
					|| scanMapTiles[centerIndex + 1][centerIndex].getTerrain() == Terrain.SAND) {
				// check whether south is obstacle
				if (scanMapTiles[centerIndex][centerIndex + 1].getHasRover()
						|| scanMapTiles[centerIndex][centerIndex + 1]
								.getTerrain() == Terrain.ROCK
						|| scanMapTiles[centerIndex][centerIndex + 1]
								.getTerrain() == Terrain.NONE
						|| scanMapTiles[centerIndex][centerIndex + 1]
								.getTerrain() == Terrain.FLUID
						|| scanMapTiles[centerIndex][centerIndex + 1]
								.getTerrain() == Terrain.SAND) {
					// check whether north is obstacle
					if (scanMapTiles[centerIndex][centerIndex - 1]
							.getHasRover()
							|| scanMapTiles[centerIndex][centerIndex - 1]
									.getTerrain() == Terrain.ROCK
							|| scanMapTiles[centerIndex][centerIndex - 1]
									.getTerrain() == Terrain.NONE
							|| scanMapTiles[centerIndex][centerIndex - 1]
									.getTerrain() == Terrain.FLUID
							|| scanMapTiles[centerIndex][centerIndex - 1]
									.getTerrain() == Terrain.SAND) {
						out.println("MOVE W");
						System.out.println("ROVER_12 request move W");
						goingSouth = false;
						goingEast = false;
						goingNorth = false;
						goingWest = true;
					} else {
						out.println("MOVE N");
						System.out.println("ROVER_12 request move N");
						goingSouth = false;
						goingEast = false;
						goingNorth = true;
						goingWest = false;

					}

				} else {
					out.println("MOVE S");
					System.out.println("ROVER_12 request move S");
					goingSouth = true;
					goingEast = false;
					goingNorth = false;
					goingWest = false;

				}

			}
			// when no obstacle is in next move to east
			else {
				out.println("MOVE E");
				System.out.println("ROVER_12 request move E");
				goingSouth = false;
				goingEast = true;
				goingNorth = false;
				goingWest = false;

			}

		}

		else if (goingWest) {
			// if next move to west is an obstacle
			if (scanMapTiles[centerIndex - 1][centerIndex].getHasRover()
					|| scanMapTiles[centerIndex - 1][centerIndex].getTerrain() == Terrain.ROCK
					|| scanMapTiles[centerIndex - 1][centerIndex].getTerrain() == Terrain.NONE
					|| scanMapTiles[centerIndex - 1][centerIndex].getTerrain() == Terrain.FLUID
					|| scanMapTiles[centerIndex - 1][centerIndex].getTerrain() == Terrain.SAND) {
				// check whether south is obstacle
				if (scanMapTiles[centerIndex][centerIndex + 1].getHasRover()
						|| scanMapTiles[centerIndex][centerIndex + 1]
								.getTerrain() == Terrain.ROCK
						|| scanMapTiles[centerIndex][centerIndex + 1]
								.getTerrain() == Terrain.NONE
						|| scanMapTiles[centerIndex][centerIndex + 1]
								.getTerrain() == Terrain.FLUID
						|| scanMapTiles[centerIndex][centerIndex + 1]
								.getTerrain() == Terrain.SAND) {
					// check whether north is obstacle
					if (scanMapTiles[centerIndex][centerIndex - 1]
							.getHasRover()
							|| scanMapTiles[centerIndex][centerIndex - 1]
									.getTerrain() == Terrain.ROCK
							|| scanMapTiles[centerIndex][centerIndex - 1]
									.getTerrain() == Terrain.NONE
							|| scanMapTiles[centerIndex][centerIndex - 1]
									.getTerrain() == Terrain.FLUID
							|| scanMapTiles[centerIndex][centerIndex - 1]
									.getTerrain() == Terrain.SAND) {
						out.println("E");
						System.out.println("ROVER_12 request move E");
						goingSouth = false;
						goingEast = true;
						goingNorth = false;
						goingWest = false;
					} else {
						out.println("MOVE N");
						System.out.println("ROVER_12 request move N");
						goingSouth = false;
						goingEast = false;
						goingNorth = true;
						goingWest = false;

					}

				} else {
					out.println("MOVE S");
					System.out.println("ROVER_12 request move S");
					goingSouth = true;
					goingEast = false;
					goingNorth = false;
					goingWest = false;

				}

			}
			// when no obstacle is in next move to west
			else {
				out.println("MOVE W");
				System.out.println("ROVER_12 request move W");
				goingSouth = false;
				goingEast = false;
				goingNorth = false;
				goingWest = true;

			}

		}

		else if (goingSouth) {

			// check whether south is obstacle
			if (scanMapTiles[centerIndex][centerIndex + 1].getHasRover()
					|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.ROCK
					|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.NONE
					|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.FLUID
					|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.SAND) {
				// if next move to west is an obstacle

				if (scanMapTiles[centerIndex - 1][centerIndex].getHasRover()
						|| scanMapTiles[centerIndex - 1][centerIndex]
								.getTerrain() == Terrain.ROCK
						|| scanMapTiles[centerIndex - 1][centerIndex]
								.getTerrain() == Terrain.NONE
						|| scanMapTiles[centerIndex - 1][centerIndex]
								.getTerrain() == Terrain.FLUID
						|| scanMapTiles[centerIndex - 1][centerIndex]
								.getTerrain() == Terrain.SAND) {
					// check whether east is obstacle
					if (scanMapTiles[centerIndex + 1][centerIndex]
							.getHasRover()
							|| scanMapTiles[centerIndex + 1][centerIndex]
									.getTerrain() == Terrain.ROCK
							|| scanMapTiles[centerIndex + 1][centerIndex]
									.getTerrain() == Terrain.NONE
							|| scanMapTiles[centerIndex + 1][centerIndex]
									.getTerrain() == Terrain.FLUID
							|| scanMapTiles[centerIndex + 1][centerIndex]
									.getTerrain() == Terrain.SAND) {
						out.println("MOVE N");
						System.out.println("ROVER_12 request move N");
						goingSouth = false;
						goingEast = false;
						goingNorth = true;
						goingWest = false;
					} else {
						out.println("MOVE E");
						System.out.println("ROVER_12 request move E");
						goingSouth = false;
						goingEast = true;
						goingNorth = false;
						goingWest = false;

					}

				} else {
					out.println("MOVE W");
					System.out.println("ROVER_12 request move W");
					goingSouth = false;
					goingEast = false;
					goingNorth = false;
					goingWest = true;

				}

			}
			// when no obstacle is in next move to south
			else {
				out.println("MOVE S");
				System.out.println("ROVER_12 request move S");
				goingSouth = true;
				goingEast = false;
				goingNorth = false;
				goingWest = false;

			}

		}

		else if (goingNorth) {

			// check whether north is obstacle
			if (scanMapTiles[centerIndex][centerIndex - 1].getHasRover()
					|| scanMapTiles[centerIndex][centerIndex - 1].getTerrain() == Terrain.ROCK
					|| scanMapTiles[centerIndex][centerIndex - 1].getTerrain() == Terrain.NONE
					|| scanMapTiles[centerIndex][centerIndex - 1].getTerrain() == Terrain.FLUID
					|| scanMapTiles[centerIndex][centerIndex - 1].getTerrain() == Terrain.SAND) {
				// if next move to west is an obstacle

				if (scanMapTiles[centerIndex - 1][centerIndex].getHasRover()
						|| scanMapTiles[centerIndex - 1][centerIndex]
								.getTerrain() == Terrain.ROCK
						|| scanMapTiles[centerIndex - 1][centerIndex]
								.getTerrain() == Terrain.NONE
						|| scanMapTiles[centerIndex - 1][centerIndex]
								.getTerrain() == Terrain.FLUID
						|| scanMapTiles[centerIndex - 1][centerIndex]
								.getTerrain() == Terrain.SAND) {
					// check whether east is obstacle
					if (scanMapTiles[centerIndex + 1][centerIndex]
							.getHasRover()
							|| scanMapTiles[centerIndex + 1][centerIndex]
									.getTerrain() == Terrain.ROCK
							|| scanMapTiles[centerIndex + 1][centerIndex]
									.getTerrain() == Terrain.NONE
							|| scanMapTiles[centerIndex + 1][centerIndex]
									.getTerrain() == Terrain.FLUID
							|| scanMapTiles[centerIndex + 1][centerIndex]
									.getTerrain() == Terrain.SAND) {
						out.println("MOVE S");
						System.out.println("ROVER_12 request move S");
						goingSouth = true;
						goingEast = false;
						goingNorth = false;
						goingWest = false;
					} else {
						out.println("MOVE E");
						System.out.println("ROVER_12 request move E");
						goingSouth = false;
						goingEast = true;
						goingNorth = false;
						goingWest = false;

					}

				} else {
					out.println("MOVE W");
					System.out.println("ROVER_12 request move W");
					goingSouth = false;
					goingEast = false;
					goingNorth = false;
					goingWest = true;

				}

			}
			// when no obstacle is in next move to north
			else {
				out.println("MOVE N");
				System.out.println("ROVER_12 request move N");
				goingSouth = false;
				goingEast = false;
				goingNorth = true;
				goingWest = false;

			}

		}
	}

	// ################ Support Methods ###########################

	private void clearReadLineBuffer() throws IOException {
		while (in.ready()) {
			// System.out.println("ROVER_12 clearing readLine()");
			String garbage = in.readLine();
		}
	}

	// method to retrieve a list of the rover's equipment from the server
	private ArrayList<String> getEquipment() throws IOException {
		// System.out.println("ROVER_12 method getEquipment()");
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		out.println("EQUIPMENT");

		String jsonEqListIn = in.readLine(); // grabs the string that was
												// returned first
		if (jsonEqListIn == null) {
			jsonEqListIn = "";
		}
		StringBuilder jsonEqList = new StringBuilder();
		// System.out.println("ROVER_12 incomming EQUIPMENT result - first readline: "
		// + jsonEqListIn);

		if (jsonEqListIn.startsWith("EQUIPMENT")) {
			while (!(jsonEqListIn = in.readLine()).equals("EQUIPMENT_END")) {
				if (jsonEqListIn == null) {
					break;
				}
				// System.out.println("ROVER_12 incomming EQUIPMENT result: " +
				// jsonEqListIn);
				jsonEqList.append(jsonEqListIn);
				jsonEqList.append("\n");
				// System.out.println("ROVER_12 doScan() bottom of while");
			}
		} else {
			// in case the server call gives unexpected results
			clearReadLineBuffer();
			return null; // server response did not start with "EQUIPMENT"
		}

		String jsonEqListString = jsonEqList.toString();
		ArrayList<String> returnList;
		returnList = gson.fromJson(jsonEqListString,
				new TypeToken<ArrayList<String>>() {
				}.getType());
		// System.out.println("ROVER_12 returnList " + returnList);

		return returnList;
	}

	// sends a SCAN request to the server and puts the result in the scanMap
	// array
	// sends a SCAN request to the server and puts the result in the scanMap
	// array
	public void doScan() throws IOException {
		// System.out.println("ROVER_12 method doScan()");
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		out.println("SCAN");

		String jsonScanMapIn = in.readLine(); // grabs the string that was
												// returned first
		if (jsonScanMapIn == null) {
			System.out.println("ROVER_12 check connection to server");
			jsonScanMapIn = "";
		}
		StringBuilder jsonScanMap = new StringBuilder();
		System.out.println("ROVER_12 incomming SCAN result - first readline: "
				+ jsonScanMapIn);

		if (jsonScanMapIn.startsWith("SCAN")) {
			while (!(jsonScanMapIn = in.readLine()).equals("SCAN_END")) {
				// System.out.println("ROVER_12 incomming SCAN result: " +
				// jsonScanMapIn);
				jsonScanMap.append(jsonScanMapIn);
				jsonScanMap.append("\n");
				// System.out.println("ROVER_12 doScan() bottom of while");
			}
		} else {
			// in case the server call gives unexpected results
			clearReadLineBuffer();
			return; // server response did not start with "SCAN"
		}
		// System.out.println("ROVER_12 finished scan while");

		String jsonScanMapString = jsonScanMap.toString();
		// convert from the json string back to a ScanMap object
		scanMap = gson.fromJson(jsonScanMapString, ScanMap.class);
	}

	private void copyScanArrayToMapLog() {

		// set the pointer object to currently scanned ScanMap
		MapTile[][] ptrScanMap = scanMap.getScanMap();
		Terrain ter;
		Science sci;
		int elev;
		boolean hasR;
		int scanMapHalfSize = (int) Math.floor(ptrScanMap.length / 2.);

		// FIXME - there's a problem with the map copy
		for (int i = 0; i < ptrScanMap.length; i++) {
			for (int j = 0; j < ptrScanMap.length; j++) {
				if (withinTheGrid(currentLoc.getYpos() - 5 + i,
						currentLoc.getXpos() - 5 + j, mapTileLog.length)) {
					ter = ptrScanMap[i][j].getTerrain();
					sci = ptrScanMap[i][j].getScience();
					elev = ptrScanMap[i][j].getElevation();
					hasR = ptrScanMap[i][j].getHasRover();
					if (mapTileLog[currentLoc.getYpos() - 5 + i][currentLoc
							.getXpos() - 5 + j] == null) {
						mapTileLog[currentLoc.getYpos() - 5 + i][currentLoc
								.getXpos() - 5 + j] = new MapTile(ter, sci,
								elev, hasR);
					}
				}
			}
		}
		debugPrintMapTileArray(mapTileLog);
	}

	public void debugPrintMapTileArray(MapTile[][] mapTileArray) {

		int edgeSize = mapTileArray.length;
		System.out.println("edge size: " + edgeSize);
		for (int k = 0; k < edgeSize + 2; k++) {
			System.out.print("--");
		}

		System.out.print("\n");

		for (int j = 0; j < edgeSize; j++) {

			System.out.print("j=" + j + "\t");

			System.out.print("| ");
			for (int i = 0; i < edgeSize; i++) {
				if (mapTileArray[i][j] == null) {
					System.out.print("n");
				}
				// check and print edge of map has first priority
				else if (mapTileArray[i][j].getTerrain().toString()
						.equals("NONE")) {
					System.out.print("XX");

					// next most important - print terrain and/or science
					// locations
					// terrain and science
				} else if (!(mapTileArray[i][j].getTerrain().toString()
						.equals("SOIL"))
						&& !(mapTileArray[i][j].getScience().toString()
								.equals("NONE"))) {
					// both terrain and science

					System.out.print(mapTileArray[i][j].getTerrain().toString()
							.substring(0, 1)
							+ mapTileArray[i][j].getScience().getSciString());
					// just terrain
				} else if (!(mapTileArray[i][j].getTerrain().toString()
						.equals("SOIL"))) {
					System.out.print(mapTileArray[i][j].getTerrain().toString()
							.substring(0, 1)
							+ " ");
					// just science
				} else if (!(mapTileArray[i][j].getScience().toString()
						.equals("NONE"))) {
					System.out.print(" "
							+ mapTileArray[i][j].getScience().getSciString());

					// if still empty check for rovers and print them
				} else if (mapTileArray[i][j].getHasRover()) {
					System.out.print("[]");

					// nothing here so print nothing
				} else {
					System.out.print("  ");
				}
			}
			System.out.print(" |\n");
		}
		for (int k = 0; k < edgeSize + 2; k++) {
			System.out.print("--");
		}
		System.out.print("\n");
	}

	public boolean withinTheGrid(int i, int j, int arrayLength) {
		return i >= 0 && j >= 0 && i < arrayLength && j < arrayLength;
	}

	private Coord requestTargetLoc() throws IOException {

		// setCurrentLoc();

		out.println("TARGET_LOC " + currentLoc.getXpos() + " "
				+ currentLoc.getYpos());
		line = in.readLine();

		if (line == null || line == "") {
			// System.out.println("ROVER_12 check connection to server");
			line = "";
		}

		if (line.startsWith("TARGET")) {
			targetLocation = extractTargetLOC(line);
		}
		return targetLocation;
	}

	public static Coord extractTargetLOC(String sStr) {
		sStr = sStr.substring(11);
		if (sStr.lastIndexOf(" ") != -1) {
			String xStr = sStr.substring(0, sStr.lastIndexOf(" "));
			// System.out.println("extracted xStr " + xStr);

			String yStr = sStr.substring(sStr.lastIndexOf(" ") + 1);
			// System.out.println("extracted yStr " + yStr);
			return new Coord(Integer.parseInt(xStr), Integer.parseInt(yStr));
		}
		return null;
	}

	private Coord requestStartLoc() throws IOException {

		setCurrentLoc();
		
		line = in.readLine();
		System.out.println(line);

		if (line == null || line == "") {
			System.out.println("ROVER_12 check connection to server");
			line = "";
		}

		//
		System.out.println();
		if (line.startsWith("START")) {
			rovergroupStartPosition = extractStartLOC(line);
		}
		return rovergroupStartPosition;
	}

	public static Coord extractCurrLOC(String sStr) {
		sStr = sStr.substring(4);
		if (sStr.lastIndexOf(" ") != -1) {
			String xStr = sStr.substring(0, sStr.lastIndexOf(" "));
			// System.out.println("extracted xStr " + xStr);

			String yStr = sStr.substring(sStr.lastIndexOf(" ") + 1);
			System.out.println("extracted yStr " + yStr);
			return new Coord(Integer.parseInt(xStr), Integer.parseInt(yStr));
		}
		return null;
	}

	public static Coord extractStartLOC(String sStr) {

		sStr = sStr.substring(10);

		if (sStr.lastIndexOf(" ") != -1) {
			String xStr = sStr.substring(0, sStr.lastIndexOf(" "));
			// System.out.println("extracted xStr " + xStr);

			String yStr = sStr.substring(sStr.lastIndexOf(" ") + 1);
			// System.out.println("extracted yStr " + yStr);
			return new Coord(Integer.parseInt(xStr), Integer.parseInt(yStr));
		}
		return null;
	}

	private Coord setCurrentLoc() throws IOException {
		String line;

		out.println("LOC");
		line = in.readLine();
		if (line == null) {
			// System.out.println("ROVER_12 check connection to server");
			line = "";
		}
		if (line.startsWith("LOC")) {
			// loc = line.substring(4);
			currentLoc = extractCurrLOC(line);
		}
	}

	private Coord getCurrentLoc() throws IOException {
		String line;
		Coord loc = null;

		out.println("LOC");
		line = in.readLine();
		if (line == null) {
			// System.out.println("ROVER_12 check connection to server");
			line = "";
		}
		if (line.startsWith("LOC")) {
			// loc = line.substring(4);
			loc = extractCurrLOC(line);
		}
		return loc;
	}

	/**
	 * Runs the client
	 */
	public static void main(String[] args) throws Exception {
		ROVER_12 client = new ROVER_12();
		client.run();
	}
}
