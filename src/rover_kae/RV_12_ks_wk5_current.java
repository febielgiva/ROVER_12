package rover_kae;

/*  scan map size is 11 x 11
 comment */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import swarmBots.NextMoveModel;
import temp.ROVER_12_ks;

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
 * allowed # request to the server per sec = 500 2 req / sec
 */

public class RV_12_ks_wk5_current {
	
	BufferedReader in;
	PrintWriter out;
	String rovername, line, currDir;
	ScanMap scanMap;
	int sleepTime;
	String SERVER_ADDRESS = "localhost";
	static final int PORT_ADDRESS = 9537;	
	
	Socket socket;
	Random rd = new Random();
	Coord currentLoc, previousLoc, rovergroupStartPosition = null, targetLocation = null;
	String currentDir = "";
	Set<String> blockedDirs = new HashSet<String>();
	Set<String> openDirs = new HashSet<String>();
	String[] cardinals = new String[4];	
	static String myJSONStringBackupofMap;
	public ArrayList<Coord> pathMap = new ArrayList<Coord>();
	Map<Coord, MapTile> mapTileLog = new HashMap<Coord, MapTile>();


	public RV_12_ks_wk5_current() {
		// constructor
				System.out.println("ROVER_12 rover object constructed");
				rovername = "ROVER_12";
				SERVER_ADDRESS = "localhost";
				// this should be a safe but slow timer value
				sleepTime = 300; // in milliseconds - smaller is faster, but the server
									// will cut connection if it is too small
	}

	public RV_12_ks_wk5_current(String serverAddress) {
		// constructor
				System.out.println("ROVER_12 rover object constructed");
				rovername = "ROVER_12";
				SERVER_ADDRESS = serverAddress;
				sleepTime = 300; // in milliseconds - smaller is faster, but the server
									// will cut connection if it is too small
	}

	private void run() throws IOException, InterruptedException {

		// Make connection to SwarmServer and initialize streams
		Socket socket = null;
		int pedometer = 0;
		try {
			socket = new Socket(SERVER_ADDRESS, PORT_ADDRESS);

			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
			

			// ********* Start a communication to the server *********
			submiteRoverName();
			

			// ********* Rover logic setup *********
			/**
			 * Get initial values that won't change
			 */
			String line = "";
			ArrayList<String> equipment = new ArrayList<String>();
			equipment = getEquipment();
			

			// **** Request START_LOC Location from SwarmServer ****
			rovergroupStartPosition = requestStartLoc(socket);
			System.out.println(rovername + " START_LOC "
					+ rovergroupStartPosition);
			// Thread.sleep(10000);
			

			// **** Request TARGET_LOC Location from SwarmServer ****
			targetLocation = requestTargetLoc(socket);
			System.out.println(rovername + " TARGET_LOC " + targetLocation);
			// Thread.sleep(10000);
			boolean goingSouth = false;
			boolean goingEast = true;
			boolean goingNorth = false;
			boolean goingWest = true;
			boolean stuck = false; // just means it did not change locations
									// between requests,

			Coord previousLoc;
			int i = 0;
		//	switchDir_ESWS();
			// **** get equipment listing ****
			equipment = getEquipment();
			System.out.println(rovername + " equipment list results "
					+ equipment + "\n");

			/**
			 * #### Rover controller process loop ####
			 */
			currentLoc = setCurrentLoc(currentLoc);
			while (true) {

				previousLoc = currentLoc.clone();

				System.out.println("curr loc (before a move request): "
						+ currentLoc);
				System.out.println("prev loc (before a move request): "
						+ previousLoc);

				/*
				 * 0. check to see if rover 12 has moved (the server has
				 * responded to move-request) a) moved - go to 1. b) not moved,
				 * Thread.sleep(800), continue to the next iteration of the loop
				 */
				doScanOriginal();
				/* 1. scan map tile (forget about mapLog b/c of JsonCopy) */

				/*
				 * 2. check for stuckness (can be checked by observing 11 x 11
				 * map tile)
				 */

				/* 3. check 3 steps ahead (sands or rock?) */

				/* 4-a. move if next 3 tiles in current direction is clear */

				/* 4-b. switch direction if next 3 tiles contains sand or rock */

				/* end the controller process loop */

				scanMap.debugPrintMap();
				
				i = (i > 2) ? 0 : (i + 1);
				move(cardinals[i]);
				System.out.println("curr direction:" + cardinals[i] + "\ti="
						+ i);

				setCurrentLoc(currentLoc);
				System.out.println("curr loc (after a move request): "
						+ currentLoc);

				pedometer = incrementPedometer(pedometer, previousLoc);

				Thread.sleep(3000);

			}// end of controller process loop
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
	private int incrementPedometer(int pedometer, Coord previousLoc) {
		// increment pedometer
		return pedometer += Math.abs(currentLoc.getXpos()
				- previousLoc.getXpos())
				+ Math.abs(currentLoc.getYpos() - previousLoc.getYpos());
	}

	private void submiteRoverName() throws IOException {
		// Process all messages from server, wait until server requests
		// Rover ID
		// name - Return Rover Name to complete connection
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
	}

	private void doThisWhenStuck_4stepToOpenDir(Coord currentLoc,
			MapTile[][] scanMapTiles) throws InterruptedException, IOException {

		String currentDir;
		findOpenDirs(currentLoc);
		currentDir = openDirs.toArray(new String[1])[0];
		for (int i = 0; i < 4; i++) {
			move(currentDir);
			Thread.sleep(300);
		}
	}

	private void doThisWhenStuck(Coord currentLoc, MapTile[][] scanMapTiles)
			throws InterruptedException, IOException {

		for (int i = 0; i < 4; i++) {
			aStepAwayFromClutter();
			Thread.sleep(300);
		}
	}

	private void makeConnAndInitStream() throws UnknownHostException,
			IOException {
		socket = new Socket(SERVER_ADDRESS, PORT_ADDRESS); // set port
															// here
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);
	}

	private void processServerMsgAndWaitForIDRequestCall() throws IOException {
		submiteRoverName();
	}

	private void resetOpenDir() {
		openDirs.add("E");
		openDirs.add("W");
		openDirs.add("S");
		openDirs.add("N");
	}

	private void findOpenDirs(Coord currentLoc) {
		// KSTD - do I need to run findBlockedDirs every time I do getopendir()?
		resetOpenDir();
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
	}

	// KSTD - very ugly. Does anyone know how to make this better?
	private boolean isSand(String direction) throws IOException {

		int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
		int x = centerIndex, y = centerIndex;
		if (direction == "S") {
			System.out.print("heading south\t");
			x = centerIndex + 1;
		} else if (direction == "N") {
			System.out.print("heading north\t");
			x = centerIndex - 1;
		} else if (direction == "E") {
			System.out.print("heading east\t");
			y = centerIndex + 1;
		} else {
			System.out.print("heading west\t");
			y = centerIndex - 1;
		}
		// Checks whether there is sand in the next tile
		if (scanMap.getScanMap()[x][y].getTerrain() == Terrain.SAND
				|| scanMap.getScanMap()[x + 1][y].getTerrain() == Terrain.SAND
				|| scanMap.getScanMap()[x - 1][y].getTerrain() == Terrain.SAND
				|| scanMap.getScanMap()[x][y + 1].getTerrain() == Terrain.SAND
				|| scanMap.getScanMap()[x][y - 1].getTerrain() == Terrain.SAND
				|| scanMap.getScanMap()[x + 1][y + 1].getTerrain() == Terrain.SAND
				|| scanMap.getScanMap()[x + 1][y - 1].getTerrain() == Terrain.SAND
				|| scanMap.getScanMap()[x - 1][y + 1].getTerrain() == Terrain.SAND
				|| scanMap.getScanMap()[x - 1][y - 1].getTerrain() == Terrain.SAND
				|| scanMap.getScanMap()[x + 2][y].getTerrain() == Terrain.SAND
				|| scanMap.getScanMap()[x - 2][y].getTerrain() == Terrain.SAND
				|| scanMap.getScanMap()[x][y + 2].getTerrain() == Terrain.SAND
				|| scanMap.getScanMap()[x][y - 2].getTerrain() == Terrain.SAND
				|| scanMap.getScanMap()[x + 2][y + 2].getTerrain() == Terrain.SAND
				|| scanMap.getScanMap()[x + 2][y - 2].getTerrain() == Terrain.SAND
				|| scanMap.getScanMap()[x - 2][y + 2].getTerrain() == Terrain.SAND
				|| scanMap.getScanMap()[x - 2][y - 2].getTerrain() == Terrain.SAND
				|| scanMap.getScanMap()[x + 3][y].getTerrain() == Terrain.SAND
				|| scanMap.getScanMap()[x - 3][y].getTerrain() == Terrain.SAND
				|| scanMap.getScanMap()[x][y + 3].getTerrain() == Terrain.SAND
				|| scanMap.getScanMap()[x][y - 3].getTerrain() == Terrain.SAND
				|| scanMap.getScanMap()[x + 3][y + 3].getTerrain() == Terrain.SAND
				|| scanMap.getScanMap()[x + 3][y - 3].getTerrain() == Terrain.SAND
				|| scanMap.getScanMap()[x - 3][y + 3].getTerrain() == Terrain.SAND
				|| scanMap.getScanMap()[x - 3][y - 3].getTerrain() == Terrain.SAND
				|| scanMap.getScanMap()[x + 4][y].getTerrain() == Terrain.SAND
				|| scanMap.getScanMap()[x - 4][y].getTerrain() == Terrain.SAND
				|| scanMap.getScanMap()[x][y + 4].getTerrain() == Terrain.SAND
				|| scanMap.getScanMap()[x][y - 4].getTerrain() == Terrain.SAND
				|| scanMap.getScanMap()[x + 4][y + 4].getTerrain() == Terrain.SAND
				|| scanMap.getScanMap()[x + 4][y - 4].getTerrain() == Terrain.SAND
				|| scanMap.getScanMap()[x - 4][y + 4].getTerrain() == Terrain.SAND
				|| scanMap.getScanMap()[x - 4][y - 4].getTerrain() == Terrain.SAND) {
			System.out.println("That's sand!!!");
			return true;
		}

		return false;
	}

	// **********************************************
	// KSTD - implement it
	private void aStepAwayFromClutter() throws IOException {
		findOpenDirs(currentLoc);

		int tracker = 0;
		while (tracker < 1) {
			for (String dir : openDirs) {
				move(dir);
			}
		}
	}

	// KSTD - implement it
	private void findBlockedDirs(Coord currentLoc) {
//		int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
//
//		System.out.println("scan map size ( findBlockedDirs() ): "
//				+ scanMap.getEdgeSize());
//
//		tempMapTiles = scanMap.getScanMap();
//
//		// debugPrintDirs(scanMapTiles, centerIndex);
//		System.out.println("scanMapTiles: " + scanMap.getScanMap());
//		if (withinTheGrid(centerIndex, centerIndex - 1, tempMapTiles.length)
//				&& tempMapTiles[centerIndex][centerIndex - 1].getHasRover()
//				|| tempMapTiles[centerIndex][centerIndex - 1].getTerrain() == Terrain.ROCK
//				|| tempMapTiles[centerIndex][centerIndex - 1].getTerrain() == Terrain.NONE
//				|| tempMapTiles[centerIndex][centerIndex - 1].getTerrain() == Terrain.SAND) {
//			System.out.println("north blocked");
//			blockedDirs.add("N");
//		}
//
//		if (withinTheGrid(centerIndex, centerIndex + 1, tempMapTiles.length)
//				&& tempMapTiles[centerIndex][centerIndex + 1].getHasRover()
//				|| tempMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.ROCK
//				|| tempMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.NONE
//				|| tempMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.SAND) {
//			System.out.println("south blocked");
//			blockedDirs.add("S");
//		}
//
//		if (withinTheGrid(centerIndex + 1, centerIndex, tempMapTiles.length)
//				&& tempMapTiles[centerIndex + 1][centerIndex].getHasRover()
//				|| tempMapTiles[centerIndex + 1][centerIndex].getTerrain() == Terrain.ROCK
//				|| tempMapTiles[centerIndex + 1][centerIndex].getTerrain() == Terrain.NONE
//				|| tempMapTiles[centerIndex + 1][centerIndex].getTerrain() == Terrain.SAND) {
//			System.out.println("east blocked");
//			blockedDirs.add("E");
//		}
//
//		if (withinTheGrid(centerIndex - 1, centerIndex, tempMapTiles.length)
//				&& tempMapTiles[centerIndex - 1][centerIndex].getHasRover()
//				|| tempMapTiles[centerIndex - 1][centerIndex].getTerrain() == Terrain.ROCK
//				|| tempMapTiles[centerIndex - 1][centerIndex].getTerrain() == Terrain.NONE
//				|| tempMapTiles[centerIndex - 1][centerIndex].getTerrain() == Terrain.SAND) {
//			System.out.println("west blocked");
//			blockedDirs.add("W");
//		}

	}

	private void debugPrintDirs(MapTile[][] scanMapTiles, int centerIndex) {
		System.out.println("center: "
				+ scanMapTiles[centerIndex][centerIndex].getHasRover());
		System.out
				.println("s: "
						+ scanMapTiles[centerIndex][centerIndex + 1]
								.getElevation()
						+ " "
						+ scanMapTiles[centerIndex][centerIndex + 1]
								.getHasRover()
						+ " "
						+ scanMapTiles[centerIndex][centerIndex + 1]
								.getTerrain()
						+ " "
						+ scanMapTiles[centerIndex][centerIndex + 1]
								.getScience());
		System.out
				.println("n: "
						+ scanMapTiles[centerIndex][centerIndex - 1]
								.getElevation()
						+ " "
						+ scanMapTiles[centerIndex][centerIndex - 1]
								.getHasRover()
						+ " "
						+ scanMapTiles[centerIndex][centerIndex - 1]
								.getTerrain()
						+ " "
						+ scanMapTiles[centerIndex][centerIndex - 1]
								.getScience());
		System.out
				.println("w: "
						+ scanMapTiles[centerIndex - 1][centerIndex]
								.getElevation()
						+ " "
						+ scanMapTiles[centerIndex - 1][centerIndex]
								.getHasRover()
						+ " "
						+ scanMapTiles[centerIndex - 1][centerIndex]
								.getTerrain()
						+ " "
						+ scanMapTiles[centerIndex - 1][centerIndex]
								.getScience());
		System.out
				.println("e: "
						+ scanMapTiles[centerIndex + 1][centerIndex]
								.getElevation()
						+ " "
						+ scanMapTiles[centerIndex + 1][centerIndex]
								.getHasRover()
						+ " "
						+ scanMapTiles[centerIndex + 1][centerIndex]
								.getTerrain()
						+ " "
						+ scanMapTiles[centerIndex + 1][centerIndex]
								.getScience());

		blockedDirs.clear();
		if (currentDir.equals("E")) {
			if (scanMapTiles[centerIndex][centerIndex + 1].getHasRover()
					|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.ROCK
					|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.NONE
					|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.SAND) {
				System.out.println("east blocked");
				blockedDirs.add("E");
			}
		}
	}

	// TODO - we will not need it. (we don't carry any of the excavation tools)
	private void harvestScience() {
	}

	private void debugCompareScanMapAndMapTileLog(int x, int y, int size) {

		int scanRange = 11;

		try {
			doScan();
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < scanRange; i++) {
			for (int j = 0; j < scanRange; j++) {
				System.out.print("i,j = " + i + ", " + j + "\t");
				System.out.print("terr: "
						+ scanMap.getScanMap()[i][j].getTerrain() + "\t");
				System.out.print("sci: "
						+ scanMap.getScanMap()[i][j].getScience() + "\n");
			}
		}
		System.out.println("************ current location " + currentLoc
				+ "******************************");
		
	}

	private void sinusoidal(String[] cardinals) throws InterruptedException,
			IOException {

		int waveLength = 3, waveHeight = 6, steps = waveLength;
		cardinals[0] = "E";
		cardinals[1] = "S";
		cardinals[2] = "E";
		cardinals[3] = "N";
		for (int i = 0; i < cardinals.length; i++) {

			currentDir = cardinals[i];
			if (currentDir.equals("E") || currentDir.equals("W")) {
				steps = waveLength;
			} else {
				steps = waveHeight;
			}

			for (int j = 0; j < steps; j++) {
				move(currentDir);
				Thread.sleep(300);
			}
		}
	}

	private boolean isStuck(Coord curr, Coord prev) {
		return curr.equals(prev);
	}

// ################ Support Methods ###########################

	// sends a SCAN request to the server and puts the result in the scanMap
	// array
	private void doScan() throws IOException {
		System.out.println("ROVER_12 method doScan()");
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		setCurrentLoc(currentLoc);
		out.println("SCAN");

		// grabs the string that was returned first
		String jsonScanMapIn = in.readLine();

		if (jsonScanMapIn == null) {
			System.out.println("ROVER_12 check connection to server");
			jsonScanMapIn = "";
		}
		StringBuilder jsonScanMap = new StringBuilder();

		if (jsonScanMapIn.startsWith("SCAN")) {
			while (!(jsonScanMapIn = in.readLine()).equals("SCAN_END")) {
				jsonScanMap.append(jsonScanMapIn);
				jsonScanMap.append("\n");
			}
		} else {
			// in case the server call gives unexpected results
			clearReadLineBuffer();
			return; // server response did not start with "SCAN"
		}

		String jsonScanMapString = jsonScanMap.toString();
		// convert from the json string back to a ScanMap object
		scanMap = gson.fromJson(jsonScanMapString, ScanMap.class);

		// set the pointer object to currently scanned ScanMap
		MapTile[][] ptrScanMap = scanMap.getScanMap();

		Terrain ter;
		Science sci;
		int elev;
		boolean hasR;

		int scanMapHalfSize = (int) Math.floor(ptrScanMap.length / 2.);

		// debug - print out 
		// System.out.println("scanMap: ");
		// debugPrintMapTileArray(ptrScanMap);
	}

	private void doScanOriginal() throws IOException {
		Gson gson = new GsonBuilder().setPrettyPrinting()
				.enableComplexMapKeySerialization().create();
		out.println("SCAN");

		String jsonScanMapIn = in.readLine();
		if (jsonScanMapIn == null) {
			System.out.println("ROVER_12 check connection to server");
			jsonScanMapIn = "";
		}
		StringBuilder jsonScanMap = new StringBuilder();

		if (jsonScanMapIn.startsWith("SCAN")) {
			while (!(jsonScanMapIn = in.readLine()).equals("SCAN_END")) {
				jsonScanMap.append(jsonScanMapIn);
				jsonScanMap.append("\n");
			}
		} else {
			// in case the server call gives unexpected results
			clearReadLineBuffer();
			return; // server response did not start with "SCAN"
		}

		String jsonScanMapString = jsonScanMap.toString();
		scanMap = gson.fromJson(jsonScanMapString, ScanMap.class);

	}

	// this takes the LOC response string, parses out the x and y values and
	// returns a Coord object
	private static Coord extractLOC(String sStr) {
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

	private int randomNum(int min, int max) {
		return rd.nextInt(max + 1) + min;
	}


	private void debugPrintMapTileArray(MapTile[][] mapTileArray) {

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

	private Coord setCurrentLoc(Coord currentLoc) throws IOException {

		String line;
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
		return currentLoc;
	}

	// to get the position of the rover--Febi added
	private List<NextMoveModel> getTheCrystalLocation(MapTile[][] scanMapTiles,
			int centerIndex) {
		List<NextMoveModel> element = new ArrayList<NextMoveModel>();
		element = null;
		for (int i = 0; i < scanMapTiles.length; i++) {
			for (int j = 0; j < scanMapTiles.length; j++) {
				if (scanMapTiles[centerIndex][centerIndex].getScience()
						.getSciString().equals("C")) {
					System.out.println("crystal found");
					element.add(new NextMoveModel(j, i, true));
				}
			}
		}
		return element;
	}

	// ####################### Support Methods #############################

	private void clearReadLineBuffer() throws IOException {
		while (in.ready()) {
			// System.out.println("ROVER_12 clearing readLine()");
			in.readLine();
		}
	}

	// method to retrieve a list of the rover's EQUIPMENT from the server
	private ArrayList<String> getEquipment() throws IOException {
		// System.out.println("ROVER_12 method getEquipment()");
		Gson gson = new GsonBuilder().setPrettyPrinting()
				.enableComplexMapKeySerialization().create();
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
	// array G12 - this raw JsonData should be used for our maptileLog?
	public void loadScanMapFromSwarmServer() throws IOException {
		// System.out.println("ROVER_12 method doScan()");
		Gson gson = new GsonBuilder().setPrettyPrinting()
				.enableComplexMapKeySerialization().create();
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
		// debug print json object to a file
		// new MyWriter( jsonScanMapString, 0); //gives a strange result -
		// prints the \n instead of newline character in the file

		// System.out.println("ROVER_12 convert from json back to ScanMap class");
		// convert from the json string back to a ScanMap object
		scanMap = gson.fromJson(jsonScanMapString, ScanMap.class);
		myJSONStringBackupofMap = jsonScanMapString;
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

	public Coord requestStartLoc(Socket soc) throws IOException {

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

	// G12 - the 3rd argument's name "size" may be too confusing. Is this okay with you guys?
	public void debugPrintMapTileArray(Map<Coord, MapTile> tiles, int xStart, int yStart ,int size) {
		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) {
				System.out.println(tiles.get(new Coord(x,y)));
			}			
		}	
	}

	private void loadMapTileIntoGlobal(MapTile[][] ptrScanMap) {
		// use a hash map
		Terrain ter;
		Science sci;
		int elev;
		boolean hasR;

		for (int i = 0; i < ptrScanMap.length; i++) {
			for (int j = 0; j < ptrScanMap.length; j++) {

				if (withinTheGrid(currentLoc.getYpos() - 5 + i,
						currentLoc.getXpos() - 5 + j, mapTileLog.size())) {
					ter = ptrScanMap[i][j].getTerrain();
					sci = ptrScanMap[i][j].getScience();
					elev = ptrScanMap[i][j].getElevation();
					hasR = ptrScanMap[i][j].getHasRover();

					mapTileLog.put(new Coord(currentLoc.getYpos() - 5 + i,
							currentLoc.getXpos() - 5 + j), new MapTile(ter,
							sci, elev, hasR));
				}
			}
		}
		// debug - print out 
		debugPrintMapTileArray(mapTileLog,0,0,10);
	}

	private void move(String dir) throws IOException {
		System.out.println("current location in move(): " + currentLoc);
		setCurrentLoc(currentLoc);
		// doScanOriginal();

		switch (dir) {

		case "E":
			if (!checkSand("E")) {
				System.out.println("request move -> E");
				moveEast();
			}
			break;
		case "W":
			if (!checkSand("W")) {
				System.out.println("request move -> W");
				moveWest();
			}
			break;
		case "N":
			if (!checkSand("N")) {
				System.out.println("request move -> N");
				moveNorth();
			}
			break;
		case "S":
			if (!checkSand("S")) {
				System.out.println("request move -> S");
				moveSouth();
			}
			break;
		default:
			break;
		}
	}

	private void moveEast() throws IOException {

		out.println("MOVE E");
		System.out.print(currentLoc + " - E -> ");
		System.out.print(currentLoc + "\n");
	}

	private void moveWest() throws IOException {
		out.println("MOVE W");
		System.out.print(currentLoc + " - W -> ");
		System.out.print(currentLoc + "\n");
	}

	private void moveNorth() throws IOException {
		out.println("MOVE N");
		System.out.print(currentLoc + " - N -> ");
		System.out.print(currentLoc + "\n");

	}

	private void moveSouth() throws IOException {
		out.println("MOVE S");
		System.out.print(currentLoc + " - S -> ");
		System.out.print(currentLoc + "\n");

	}

	// KSTD - very ugly. Does anyone know how to make this better?
	public boolean checkSand(String direction) {

		int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
		int x = centerIndex, y = centerIndex, scanRange = 2;

		for (int i = 1; i < scanRange; i++) {
			if (direction == "S")
				x = centerIndex + i;
			else if (direction == "N")
				x = centerIndex - i;
			else if (direction == "E")
				y = centerIndex + i;
			else
				y = centerIndex - i;

			// Checks whether there is sand in the next tile
			if (scanMap.getScanMap()[x][y].getTerrain() == Terrain.SAND)
				return true;
		}

		return false;
	}

	public boolean withinTheGrid(int i, int j, int arrayLength) {
		return i >= 0 && j >= 0 && i < arrayLength && j < arrayLength;
	}

	/**
	 * Runs the client
	 */
	public static void main(String[] args) throws Exception {
		RV_12_ks_wk5_current client = new RV_12_ks_wk5_current();
		client.run();
	}
}