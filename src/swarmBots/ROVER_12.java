package swarmBots;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	protected Coord targetLocation, rovergroupStartPosition, previousLoc;
	private static Coord currentLoc;

	protected int sleepTime, serverMapSize, roverLogicType=1;
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
			rovergroupStartPosition = requestStartLoc(socket);
			System.out.println(rovername + " START_LOC "
					+ rovergroupStartPosition);

			// **** Request TARGET_LOC Location from SwarmServer ****
			targetLocation = requestTargetLoc(socket);
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

			// **** Request Rover Location from SwarmServer ****
			out.println("LOC");
			line = in.readLine();
			if (line == null) {
				System.out.println(rovername + " check connection to server");
				line = "";
			}
			if (line.startsWith("LOC")) {
				// loc = line.substring(4);
				currentLoc = extractLocationFromString(line);

			}

			/**
			 * #### Rover controller process loop ####
			 */
			while (true) {

				System.out.println(rovername + " currentLoc at start: "
						+ currentLoc);

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
				roverMotionLogic1_ewsn(scanMapTiles, centerIndex, socket);

				
				// FIXME - Confusion in the terminology. help...
				blocked = currentLoc.equals(previousLoc);
				//System.out.println("ROVER_12 stuck test " + stuck);
				System.out.println("ROVER_12 blocked test " + blocked);

				if (blocked) {
					try {
						// FIXME - must determine the right amount
						for (int i = 0; i < 4; i++) {
							roverMotionLogic2_enws(centerIndex, scanMapTiles);	
						}
						
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
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
	private void roverMotionLogic1_ewsn(MapTile[][] scanMapTiles, int centerIndex,
			Socket soc) {
		boolean goingSouth = false;
		boolean goingEast = true;
		boolean goingNorth = false;
		boolean goingWest = true;
		boolean stuck = false; // just means it did not change locations
								// between requests,

		if (goingEast) {

			stepEast(scanMapTiles, centerIndex);

		} else if (goingWest) {

			stepWest(scanMapTiles, centerIndex);

		} else if (goingSouth) {

			stepSouth(scanMapTiles, centerIndex);

		} else if (goingNorth) {

			stepNorth(scanMapTiles, centerIndex);

		}

//		try {
//
//			// **** Request Rover Location from SwarmServer ****
//			out.println("LOC");
//			line = in.readLine();
//			if (line == null) {
//				System.out.println(rovername + " check connection to server");
//				line = "";
//			}
//			if (line.startsWith("LOC")) {
//				// loc = line.substring(4);
//				currentLoc = extractLocationFromString(line);
//
//			}
//			System.out.println(rovername + " currentLoc at start: "
//					+ currentLoc);
//
//			// test for stuckness
//			stuck = currentLoc.equals(previousLoc);
//
//			// FIXME - Confusion in the terminology. help...
//			boolean blocked = currentLoc.equals(previousLoc);
//			System.out.println("ROVER_12 stuck test " + stuck);
//			System.out.println("ROVER_12 blocked test " + blocked);
//
//			if (blocked) {
//				try {
//					// FIXME - must determine the right amount
//					for (int i = 0; i < 4; i++) {
//						doThisWhenStuck_aStepTowardOpenDir(currentLoc, scanMapTiles);	
//					}
//					
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//
//			// this is the Rovers HeartBeat, it regulates how fast the Rover
//			// cycles through the control loop
//			try {
//				Thread.sleep(sleepTime);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	private void stepNorth(MapTile[][] scanMapTiles, int centerIndex) {
		boolean goingSouth;
		boolean goingEast;
		boolean goingNorth;
		boolean goingWest;
		// check whether north is obstacle
		if (scanMapTiles[centerIndex][centerIndex - 1].getHasRover()
				|| scanMapTiles[centerIndex][centerIndex - 1].getTerrain() == Terrain.ROCK
				|| scanMapTiles[centerIndex][centerIndex - 1].getTerrain() == Terrain.NONE
				|| scanMapTiles[centerIndex][centerIndex - 1].getTerrain() == Terrain.FLUID
				|| scanMapTiles[centerIndex][centerIndex - 1].getTerrain() == Terrain.SAND) {
			// if next move to west is an obstacle

			if (scanMapTiles[centerIndex - 1][centerIndex].getHasRover()
					|| scanMapTiles[centerIndex - 1][centerIndex].getTerrain() == Terrain.ROCK
					|| scanMapTiles[centerIndex - 1][centerIndex].getTerrain() == Terrain.NONE
					|| scanMapTiles[centerIndex - 1][centerIndex].getTerrain() == Terrain.FLUID
					|| scanMapTiles[centerIndex - 1][centerIndex].getTerrain() == Terrain.SAND) {
				// check whether east is obstacle
				if (scanMapTiles[centerIndex + 1][centerIndex].getHasRover()
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

	private void stepSouth(MapTile[][] scanMapTiles, int centerIndex) {
		boolean goingSouth;
		boolean goingEast;
		boolean goingNorth;
		boolean goingWest;
		// check whether south is obstacle
		if (scanMapTiles[centerIndex][centerIndex + 1].getHasRover()
				|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.ROCK
				|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.NONE
				|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.FLUID
				|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.SAND) {
			// if next move to west is an obstacle

			if (scanMapTiles[centerIndex - 1][centerIndex].getHasRover()
					|| scanMapTiles[centerIndex - 1][centerIndex].getTerrain() == Terrain.ROCK
					|| scanMapTiles[centerIndex - 1][centerIndex].getTerrain() == Terrain.NONE
					|| scanMapTiles[centerIndex - 1][centerIndex].getTerrain() == Terrain.FLUID
					|| scanMapTiles[centerIndex - 1][centerIndex].getTerrain() == Terrain.SAND) {
				// check whether east is obstacle
				if (scanMapTiles[centerIndex + 1][centerIndex].getHasRover()
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

	private void stepWest(MapTile[][] scanMapTiles, int centerIndex) {
		boolean goingSouth;
		boolean goingEast;
		boolean goingNorth;
		boolean goingWest;
		// if next move to west is an obstacle
		if (scanMapTiles[centerIndex - 1][centerIndex].getHasRover()
				|| scanMapTiles[centerIndex - 1][centerIndex].getTerrain() == Terrain.ROCK
				|| scanMapTiles[centerIndex - 1][centerIndex].getTerrain() == Terrain.NONE
				|| scanMapTiles[centerIndex - 1][centerIndex].getTerrain() == Terrain.FLUID
				|| scanMapTiles[centerIndex - 1][centerIndex].getTerrain() == Terrain.SAND) {
			// check whether south is obstacle
			if (scanMapTiles[centerIndex][centerIndex + 1].getHasRover()
					|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.ROCK
					|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.NONE
					|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.FLUID
					|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.SAND) {
				// check whether north is obstacle
				if (scanMapTiles[centerIndex][centerIndex - 1].getHasRover()
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

	private void stepEast(MapTile[][] scanMapTiles, int centerIndex) {
		boolean goingSouth;
		boolean goingEast;
		boolean goingNorth;
		boolean goingWest;
		// if next move to east is an obstacle
		if (scanMapTiles[centerIndex + 1][centerIndex].getHasRover()
				|| scanMapTiles[centerIndex + 1][centerIndex].getTerrain() == Terrain.ROCK
				|| scanMapTiles[centerIndex + 1][centerIndex].getTerrain() == Terrain.NONE
				|| scanMapTiles[centerIndex + 1][centerIndex].getTerrain() == Terrain.FLUID
				|| scanMapTiles[centerIndex + 1][centerIndex].getTerrain() == Terrain.SAND) {
			// check whether south is obstacle
			if (scanMapTiles[centerIndex][centerIndex + 1].getHasRover()
					|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.ROCK
					|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.NONE
					|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.FLUID
					|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.SAND) {
				// check whether north is obstacle
				if (scanMapTiles[centerIndex][centerIndex - 1].getHasRover()
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

	private Coord requestTargetLoc(Socket soc) throws IOException {

		// **** Request Rover Location from SwarmServer ****
		out.println("LOC");
		line = in.readLine();
		if (line == null) {
			System.out.println(rovername + " check connection to server");
			line = "";
		}
		if (line.startsWith("LOC")) {
			// loc = line.substring(4);
			currentLoc = extractLocationFromString(line);

		}
		System.out.println(rovername + " currentLoc at start: " + currentLoc);

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
			System.out.println("extracted xStr " + xStr);

			String yStr = sStr.substring(sStr.lastIndexOf(" ") + 1);
			System.out.println("extracted yStr " + yStr);
			return new Coord(Integer.parseInt(xStr), Integer.parseInt(yStr));
		}
		return null;
	}

	private Coord requestStartLoc(Socket soc) throws IOException {

		// **** Request Rover Location from SwarmServer ****
		out.println("LOC");
		line = in.readLine();
		if (line == null) {
			System.out.println(rovername + " check connection to server");
			line = "";
		}
		if (line.startsWith("LOC")) {
			// loc = line.substring(4);
			currentLoc = extractLocationFromString(line);

		}
		System.out.println(rovername + " currentLoc at start: " + currentLoc);

		out.println("START_LOC " + currentLoc.getXpos() + " "
				+ currentLoc.getYpos());
		line = in.readLine();

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
			// System.out.println("extracted yStr " + yStr);
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

	private Coord copyCurrentLoc(Coord copy) throws IOException {
		String line;

		out.println("LOC");
		line = in.readLine();
		if (line == null) {
			System.out.println("ROVER_12 check connection to server");
			line = "";
		}
		if (line.startsWith("LOC")) {
			// loc = line.substring(4);
			copy = extractCurrLOC(line);
		}
		// debug
		System.out.println("copied curr loc: " + copy);
		return copy;
	}

	// this takes the server response string, parses out the x and x values and
	// returns a Coord object
	public static Coord extractLocationFromString(String sStr) {
		int indexOf;
		indexOf = sStr.indexOf(" ");
		sStr = sStr.substring(indexOf + 1);
		if (sStr.lastIndexOf(" ") != -1) {
			String xStr = sStr.substring(0, sStr.lastIndexOf(" "));
			// System.out.println("extracted xStr " + xStr);

			String yStr = sStr.substring(sStr.lastIndexOf(" ") + 1);
			// System.out.println("extracted yStr " + yStr);
			return new Coord(Integer.parseInt(xStr), Integer.parseInt(yStr));
		}
		return null;
	}

	private Set<String> findOpenDirs(Coord curr) {
		// KSTD - do I need to run findBlockedDirs every time I do getopendir()?
		Set<String> openDirs = new HashSet<String>();
		Set<String> blockedDirs = findBlockedDirs(curr);
		findBlockedDirs(currentLoc);

		// DEBUG - remove before submission
		System.out.print("\n" + "BLOCK CHECK:");
		System.out.print("\n" + "blocked:");
		for (String s : blockedDirs) {
			System.out.print(s + " ");
		}

		for (String dir : blockedDirs) {
			if (blockedDirs.contains(dir)) {
				openDirs.remove(dir);
			}
		}

		// DEBUG - remove before submission
		System.out.print("\n" + "open:");
		for (String s : openDirs) {
			System.out.print(s + " ");
		}

		return openDirs;
	}

	private Set<String> findBlockedDirs(Coord currentLoc) {
		int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
		Set<String> blockedDirs = new HashSet<String>();
		System.out.println("scan map size ( findBlockedDirs() ): "
				+ scanMap.getEdgeSize());

		MapTile[][] tempScanMap = scanMap.getScanMap();

		// debugPrintDirs(scanMapTiles, centerIndex);
		System.out.println("scanMapTiles: " + scanMap.getScanMap());
		if (withinTheGrid(centerIndex, centerIndex - 1, tempScanMap.length)
				&& tempScanMap[centerIndex][centerIndex - 1].getHasRover()
				|| tempScanMap[centerIndex][centerIndex - 1].getTerrain() == Terrain.ROCK
				|| tempScanMap[centerIndex][centerIndex - 1].getTerrain() == Terrain.NONE
				|| tempScanMap[centerIndex][centerIndex - 1].getTerrain() == Terrain.SAND) {
			System.out.println("north blocked");
			blockedDirs.add("N");
		}

		if (withinTheGrid(centerIndex, centerIndex + 1, tempScanMap.length)
				&& tempScanMap[centerIndex][centerIndex + 1].getHasRover()
				|| tempScanMap[centerIndex][centerIndex + 1].getTerrain() == Terrain.ROCK
				|| tempScanMap[centerIndex][centerIndex + 1].getTerrain() == Terrain.NONE
				|| tempScanMap[centerIndex][centerIndex + 1].getTerrain() == Terrain.SAND) {
			System.out.println("south blocked");
			blockedDirs.add("S");
		}

		if (withinTheGrid(centerIndex + 1, centerIndex, tempScanMap.length)
				&& tempScanMap[centerIndex + 1][centerIndex].getHasRover()
				|| tempScanMap[centerIndex + 1][centerIndex].getTerrain() == Terrain.ROCK
				|| tempScanMap[centerIndex + 1][centerIndex].getTerrain() == Terrain.NONE
				|| tempScanMap[centerIndex + 1][centerIndex].getTerrain() == Terrain.SAND) {
			System.out.println("east blocked");
			blockedDirs.add("E");
		}

		if (withinTheGrid(centerIndex - 1, centerIndex, tempScanMap.length)
				&& tempScanMap[centerIndex - 1][centerIndex].getHasRover()
				|| tempScanMap[centerIndex - 1][centerIndex].getTerrain() == Terrain.ROCK
				|| tempScanMap[centerIndex - 1][centerIndex].getTerrain() == Terrain.NONE
				|| tempScanMap[centerIndex - 1][centerIndex].getTerrain() == Terrain.SAND) {
			System.out.println("west blocked");
			blockedDirs.add("W");
		}
		return blockedDirs;
	}

	private boolean isStuck(String dir, Coord oldPos, Coord newPos) {
		if (dir.equals("E") || dir.equals("W")) {
			return Math.abs(oldPos.getXpos() - newPos.getXpos()) > 4;
		} else {
			return Math.abs(oldPos.getYpos() - newPos.getYpos()) > 4;
		}
	}

	private boolean hasMoved(String dir, Coord oldPos, Coord newPos) {
		if (dir.equals("E") || dir.equals("W")) {
			return Math.abs(oldPos.getXpos() - newPos.getXpos()) > 0;
		} else {
			return Math.abs(oldPos.getYpos() - newPos.getYpos()) > 0;
		}
	}

	// FIXME - there are better ways.
		private void doThisWhenStuck_aStepTowardOpenDir_old(Coord currentLoc,
				MapTile[][] maptiles) throws InterruptedException, IOException {

			String currentDir;
			Coord newPos=currentLoc.clone();
			Set<String> openDirs = findOpenDirs(currentLoc);
			currentDir = openDirs.toArray(new String[1])[0];	
			
			while (!hasMoved(currentDir, currentLoc,newPos)) {
				
					requestMove(currentDir, maptiles);
					Thread.sleep(2000);//KSTD - adjust wait length
					
				// **** Request Rover Location from SwarmServer ****
				out.println("LOC");
				line = in.readLine();
				if (line == null) {
					System.out.println(rovername
							+ " check connection to server");
					line = "";
				}
				if (line.startsWith("LOC")) {
					// loc = line.substring(4);
					newPos = extractLocationFromString(line);
				}			
			}
		}
	
		private void firmStep(Coord direction,
				MapTile[][] maptiles) throws InterruptedException, IOException {

			String currentDir;
			Coord newPos=direction.clone();
			Set<String> openDirs = findOpenDirs(direction);
			currentDir = openDirs.toArray(new String[1])[0];	
			
			while (!hasMoved(currentDir, direction,newPos)) {
				
					requestMove(currentDir, maptiles);
					Thread.sleep(2000);//KSTD - adjust wait length
					
				// **** Request Rover Location from SwarmServer ****
				out.println("LOC");
				line = in.readLine();
				if (line == null) {
					System.out.println(rovername
							+ " check connection to server");
					line = "";
				}
				if (line.startsWith("LOC")) {
					// loc = line.substring(4);
					newPos = extractLocationFromString(line);
				}			
			}
		}
		
	// FIXME - there are better ways.
	private void roverMotionLogic2_enws(int centerIndex,
			MapTile[][] maptiles) throws InterruptedException, IOException {

		boolean goingSouth = false;
		boolean goingEast = true;
		boolean goingNorth = false;
		boolean goingWest = true;
		boolean stuck = false; // just means it did not change locations
								// between requests,

		if (goingEast) {

			stepEast(maptiles, centerIndex);

		} else if (goingWest) {

			stepNorth(maptiles, centerIndex);

		} else if (goingSouth) {

			stepWest(maptiles, centerIndex);

		} else if (goingNorth) {

			stepSouth(maptiles, centerIndex);

		}
	}

	private void requestMove(String dir, MapTile[][] maptile) throws IOException {

		// **** Request Rover Location from SwarmServer ****
		out.println("LOC");
		line = in.readLine();
		if (line == null) {
			System.out.println(rovername + " check connection to server");
			line = "";
		}
		if (line.startsWith("LOC")) {
			// loc = line.substring(4);
			currentLoc = extractLocationFromString(line);

		}
		System.out.println(rovername + " currentLoc in move(): " + currentLoc);

		int centerIdx = (maptile.length - 1) / 2;
		switch (dir) {

		case "E":
			stepEast(maptile, centerIdx);
			break;
		case "W":
			System.out.println("request move -> W");
			stepWest(maptile, centerIdx);

			break;
		case "N":

			System.out.println("request move -> N");
			stepNorth(maptile, centerIdx);

			break;
		case "S":
			System.out.println("request move -> S");
			stepSouth(maptile, centerIdx);

			break;
		default:
			break;
		}
	}

	/**
	 * Runs the client
	 */
	public static void main(String[] args) throws Exception {
		ROVER_12 client = new ROVER_12();
		client.run();
	}
}
