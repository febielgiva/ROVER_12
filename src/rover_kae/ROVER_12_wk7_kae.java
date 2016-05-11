package rover_kae;

// must fix 
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONObject;

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
 */

public class ROVER_12_wk7_kae {

	private BufferedReader in;
	private PrintWriter out;
	private String rovername;
	private ScanMap scanMap;
	private int sleepTime;
	private String SERVER_ADDRESS = "localhost", line;
	static final int PORT_ADDRESS = 9537;
	static String myJSONStringBackupofMap;
	private Coord currentLoc, previousLoc, rovergroupStartPosition = null,
			targetLocation = null;
	private Map<Coord, MapTile> mapTileLog = new HashMap<Coord, MapTile>();
	// MapTile[][] mapTileLog = new MapTile[100][100];
	private List<Coord> pathMap = new ArrayList<Coord>();
	// private Deque<String> directionStack = new ArrayDeque<String>();
	private List<Coord> directionStack = new LinkedList<Coord>();

	private boolean[] cardinals = new boolean[4];

	public ROVER_12_wk7_kae() {
		// constructor
		System.out.println("ROVER_12 rover object constructed");
		rovername = "ROVER_12";
		SERVER_ADDRESS = "localhost";
		// this should be a safe but slow timer value
		sleepTime = 100; // in milliseconds - smaller is faster, but the server
							// will cut connection if it is too small
	}

	public ROVER_12_wk7_kae(String serverAddress) {
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

		// Make connection to SwarmServer and initialize streams
		Socket socket = null;
		try {
			socket = new Socket(SERVER_ADDRESS, PORT_ADDRESS);

			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);

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

			// ********* Rover logic setup *********

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
			// Thread.sleep(10000);

			// **** Request TARGET_LOC Location from SwarmServer ****
			targetLocation = requestTargetLoc(socket);
			System.out.println(rovername + " TARGET_LOC " + targetLocation);

			// debug
			// Thread.sleep(10000);

			boolean stuck = false; // just means it did not change locations
									// between requests,
			// could be velocity limit or obstruction etc. group12 - anyone
			// knows what this means?
			boolean blocked = false;

			cardinals[0] = false; // S: goingSouth
			cardinals[1] = true; // E: goingEast
			cardinals[2] = false; // N: goingNorth
			cardinals[3] = false; // W: goingWest

			int stepTrack = 0;

			// // debug
			// System.out.println(requestTimeRemaining(socket));
			// Thread.sleep(10000);

			/**
			 * #### Rover controller process loop ####
			 */
			while (true) {

				setCurrentLoc();
				previousLoc = currentLoc.clone();

				// ***** do a SCAN ******
				/*
				 * G12 - for now, it is set to load in 11 x 11 map from
				 * swarmserver, and copy it onto our g12 map log, every 4 steps
				 * that rover 12 takes. Better ideas on the iteration interval,
				 * anyone?
				 */
				if ((stepTrack++) % 4 == 0) {
					loadScanMapFromSwarmServer();
					scanMap.debugPrintMap();// debug
					debugPrintMapTileArrayText(mapTileLog, 30);
					debugPrintMapTileArray(mapTileLog);
				}

				// ***** MOVING *****
				// pull the MapTile array out of the ScanMap object
				MapTile[][] scanMapTiles = scanMap.getScanMap();
				// request(scanMapTiles);
				int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
				// tile S = y + 1; N = y - 1; E = x + 1; W = x - 1

				roverMotionLogic(cardinals, scanMapTiles, centerIndex,
						currentLoc.getXpos(), currentLoc.getYpos());
				// test for stuckness
				// KS - below line causes a crash, must be modified
				// stuck = currentLoc.equals(previousLoc);

				// System.out.println("ROVER_12 stuck test " + stuck);
				// System.out.println("ROVER_12 blocked test " + blocked);
				// System.out.println(currentLoc);

				// store rover 12 path for easy return
				pathMap.add(new Coord(currentLoc.getXpos(), currentLoc
						.getYpos()));

				// this is the Rovers HeartBeat, it regulates how fast the Rover
				// cycles through the control loop
				// Thread.sleep(sleepTime); // G12 - sleepTime has been reduced
				// to
				// 100. is that alright?

				System.out
						.println("ROVER_12 ------------ bottom process control --------------");

				// This catch block closes the open socket connection to the
				// server
			}
		} catch (Exception e) {
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

	}// END of Rover main control loop

	private void roverMotionLogic(boolean[] cardinals,
			MapTile[][] scanMapTiles, int centerIndex, int currentXPos,
			int currentYPos) throws InterruptedException, IOException {
		// ************* Febi's rover motion logic **********
		// int tempRowArray;
		// int tempColumnArray;

		// logic if going in east
		if (cardinals[1]) {
			// Checks to see if there is science on current tile, if not
			// it moves East
			System.out
					.println("ROVER_12: scanMapTiles[centerIndex][centerIndex].getScience().getSciString() "
							+ scanMapTiles[centerIndex][centerIndex]
									.getScience().getSciString());
			if (scanMapTiles[centerIndex + 1][centerIndex].getScience().equals(
					"C")) {
				// move east
				moveEast();

			} else if (scanMapTiles[centerIndex][centerIndex + 1].getScience()
					.equals("C")) {
				// move south
				moveSouth();

			} else if (scanMapTiles[centerIndex][centerIndex - 1].getScience()
					.equals("C")) {
				// move north
				moveNorth();
			} else {
				// if next move to east is an obstacle
				if ((isTowardsEastIsObsatacle(scanMapTiles, centerIndex))
						|| (isAlreadyTraveledPathTowardsEast(currentXPos,
								currentYPos))) {
					// check whether south is obstacle
					if ((isTowardsSouthIsObsatacle(scanMapTiles, centerIndex))
							|| (isAlreadyTraveledPathTowardsSouth(currentXPos,
									currentYPos))) {
						// check whether north is obstacle
						if ((isTowardsNorthIsObsatacle(scanMapTiles,
								centerIndex))
								|| (isAlreadyTraveledPathTowardsNorth(
										currentXPos, currentYPos))) {
							// move west if no obstacle or else east
							if (isTowardsWestIsObsatacle(scanMapTiles,
									centerIndex)) {
								moveEast();
							} else {
								cardinals = moveUsingPastPath(cardinals,
										currentXPos, currentYPos);
							}

						} else {
							// move north
							moveNorth();
						}
					} else {
						// move south
						moveSouth();
					}
				}
				// when no obstacle is in next move to east
				else {
					// move east
					moveEast();
				}
			}
		} else if (cardinals[3]) {
			// if next move to west is an obstacle
			if ((isTowardsWestIsObsatacle(scanMapTiles, centerIndex))
					|| (isAlreadyTraveledPathTowardsWest(currentXPos,
							currentYPos))) {
				// check whether south is obstacle
				if ((isTowardsSouthIsObsatacle(scanMapTiles, centerIndex))
						|| (isAlreadyTraveledPathTowardsSouth(currentXPos,
								currentYPos))) {
					// check whether north is obstacle
					if ((isTowardsNorthIsObsatacle(scanMapTiles, centerIndex))
							|| (isAlreadyTraveledPathTowardsNorth(currentXPos,
									currentYPos))) {
						// move east if no obstacle or else move to west
						if (isTowardsEastIsObsatacle(scanMapTiles, centerIndex)) {
							moveWest();
						} else {
							cardinals = moveUsingPastPath(cardinals,
									currentXPos, currentYPos);
						}

					} else {
						// move north
						moveNorth();
					}
				} else {
					// move south
					moveSouth();
				}
			}
			// when no obstacle is in next move to west
			else {
				// move west
				moveWest();
			}
		} else if (cardinals[0]) {

			// check whether south is obstacle
			if ((isTowardsSouthIsObsatacle(scanMapTiles, centerIndex))
					|| (isAlreadyTraveledPathTowardsSouth(currentXPos,
							currentYPos))) {
				// if next move to west is an obstacle
				if ((isTowardsWestIsObsatacle(scanMapTiles, centerIndex))
						|| (isAlreadyTraveledPathTowardsWest(currentXPos,
								currentYPos))) {
					// check whether east is obstacle
					if ((isTowardsEastIsObsatacle(scanMapTiles, centerIndex))
							|| (isAlreadyTraveledPathTowardsEast(currentXPos,
									currentYPos))) {
						// move north if no obstacle or else move in south
						if (isTowardsNorthIsObsatacle(scanMapTiles, centerIndex)) {
							moveSouth();
						} else {
							cardinals = moveUsingPastPath(cardinals,
									currentXPos, currentYPos);
						}
					} else {
						// move east
						moveEast();
					}
				} else {
					// move west
					moveWest();
				}
			}
			// when no obstacle is in next move to south
			else {
				// move south
				moveSouth();
			}
		} else if (cardinals[2]) {

			// check whether north is obstacle
			if ((isTowardsNorthIsObsatacle(scanMapTiles, centerIndex))
					|| (isAlreadyTraveledPathTowardsNorth(currentXPos,
							currentYPos))) {
				// if next move to west is an obstacle
				if ((isTowardsWestIsObsatacle(scanMapTiles, centerIndex))
						|| (isAlreadyTraveledPathTowardsWest(currentXPos,
								currentYPos))) {
					// check whether east is obstacle
					if ((isTowardsEastIsObsatacle(scanMapTiles, centerIndex))
							|| (isAlreadyTraveledPathTowardsEast(currentXPos,
									currentYPos))) {
						// move south if no obstacle or else go back to north
						if (isTowardsSouthIsObsatacle(scanMapTiles, centerIndex)) {
							moveNorth();
						} else {
							cardinals = moveUsingPastPath(cardinals,
									currentXPos, currentYPos);
						}
					} else {
						// move east
						moveEast();
					}
				} else {
					// move west
					moveWest();
				}
			}
			// when no obstacle is in next move to north
			else {
				// move north
				moveNorth();
			}
		}
	}

	private boolean isAlreadyTraveledPathTowardsWest(int currentXPos,
			int currentYPos) {
		int nextXPosition = currentXPos - 1;
		int nextYPosition = currentYPos;
		for (Coord coord : pathMap) {
			if ((coord.xpos == nextXPosition) && (coord.ypos == nextYPosition)) {
				return true;
			}
		}
		return false;
	}

	private boolean[] moveUsingPastPath(boolean[] cardinals, int currentXPos,
			int currentYPos) throws InterruptedException {
		try {
			Coord current = returnCurrentLoc(), prev = current.clone();

			for (int j = 0; (j < 10) && (j < pathMap.size()); j++) {
				for (int i = pathMap.size(); i > 0; i++) {

					while (current.equals(prev)) {
						Thread.sleep(300);
						current = returnCurrentLoc();
					}

					cardinals = assignTheMove(cardinals, pathMap.get(i),
							currentXPos, currentYPos);
					prev = current.clone();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cardinals;
	}

	private boolean[] assignTheMove(boolean[] cardinals, Coord eachCoord,
			int currentXPos, int currentYPos) throws IOException {
		if (isPastPositonIsEast(cardinals, eachCoord, currentXPos, currentYPos)) {
			moveEast();

		} else if (isPastPositonIsWest(cardinals, eachCoord, currentXPos,
				currentYPos)) {
			moveWest();

		} else if (isPastPositonIsNorth(cardinals, eachCoord, currentXPos,
				currentYPos)) {
			moveNorth();

		} else if (isPastPositonIsSouth(cardinals, eachCoord, currentXPos,
				currentYPos)) {
			moveSouth();

		}
		return cardinals;
	}

	private boolean isPastPositonIsNorth(boolean[] cardinals, Coord eachCoord,
			int currentXPos, int currentYPos) {
		int previousXPos = eachCoord.getXpos();
		int previousYPos = eachCoord.getYpos();
		if ((previousXPos == currentXPos) && (previousYPos == currentYPos - 1)) {
			return true;
		}
		return false;
	}

	private boolean isPastPositonIsWest(boolean[] cardinals, Coord eachCoord,
			int currentXPos, int currentYPos) {
		int previousXPos = eachCoord.getXpos();
		int previousYPos = eachCoord.getYpos();
		if ((previousXPos == currentXPos - 1) && (previousYPos == currentYPos)) {
			return true;
		}
		return false;
	}

	private boolean isPastPositonIsSouth(boolean[] cardinals, Coord eachCoord,
			int currentXPos, int currentYPos) {
		int previousXPos = eachCoord.getXpos();
		int previousYPos = eachCoord.getYpos();
		if ((previousXPos == currentXPos) && (previousYPos == currentYPos + 1)) {
			return true;
		}
		return false;
	}

	private boolean isPastPositonIsEast(boolean[] cardinals, Coord eachCoord,
			int currentXPos, int currentYPos) {
		int previousXPos = eachCoord.getXpos();
		int previousYPos = eachCoord.getYpos();
		if ((previousXPos == currentXPos + 1) && (previousYPos == currentYPos)) {
			return true;
		}
		return false;
	}

	private boolean isTowardsWestIsObsatacle(MapTile[][] scanMapTiles,
			int centerIndex) {
		if (scanMapTiles[centerIndex - 1][centerIndex].getHasRover()
				|| scanMapTiles[centerIndex - 1][centerIndex].getTerrain() == Terrain.ROCK
				|| scanMapTiles[centerIndex - 1][centerIndex].getTerrain() == Terrain.NONE
				|| scanMapTiles[centerIndex - 1][centerIndex].getTerrain() == Terrain.FLUID
				|| scanMapTiles[centerIndex - 1][centerIndex].getTerrain() == Terrain.SAND) {
			return true;
		}
		return false;
	}

	private boolean isAlreadyTraveledPathTowardsNorth(int currentXPos,
			int currentYPos) {
		int nextXPosition = currentXPos;
		int nextYPosition = currentYPos - 1;
		for (Coord coord : pathMap) {
			if ((coord.xpos == nextXPosition) && (coord.ypos == nextYPosition)) {
				return true;
			}
		}
		return false;
	}

	private boolean isTowardsNorthIsObsatacle(MapTile[][] scanMapTiles,
			int centerIndex) {
		if (scanMapTiles[centerIndex][centerIndex - 1].getHasRover()
				|| scanMapTiles[centerIndex][centerIndex - 1].getTerrain() == Terrain.ROCK
				|| scanMapTiles[centerIndex][centerIndex - 1].getTerrain() == Terrain.NONE
				|| scanMapTiles[centerIndex][centerIndex - 1].getTerrain() == Terrain.FLUID
				|| scanMapTiles[centerIndex][centerIndex - 1].getTerrain() == Terrain.SAND) {
			return true;
		}
		return false;
	}

	private boolean isAlreadyTraveledPathTowardsSouth(int currentXPos,
			int currentYPos) {
		int nextXPosition = currentXPos;
		int nextYPosition = currentYPos + 1;
		for (Coord coord : pathMap) {
			if ((coord.xpos == nextXPosition) && (coord.ypos == nextYPosition)) {
				return true;
			}
		}
		return false;
	}

	private boolean isTowardsSouthIsObsatacle(MapTile[][] scanMapTiles,
			int centerIndex) {
		if (scanMapTiles[centerIndex][centerIndex + 1].getHasRover()
				|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.ROCK
				|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.NONE
				|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.FLUID
				|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.SAND) {
			return true;
		}
		return false;
	}

	private boolean isAlreadyTraveledPathTowardsEast(int currentXPos,
			int currentYPos) {
		int nextXPosition = currentXPos + 1;
		int nextYPosition = currentYPos;
		for (Coord coord : pathMap) {
			if ((coord.xpos == nextXPosition) && (coord.ypos == nextYPosition)) {
				return true;
			}
		}
		return false;
	}

	private boolean isTowardsEastIsObsatacle(MapTile[][] scanMapTiles,
			int centerIndex) {
		if (scanMapTiles[centerIndex + 1][centerIndex].getHasRover()
				|| scanMapTiles[centerIndex + 1][centerIndex].getTerrain() == Terrain.ROCK
				|| scanMapTiles[centerIndex + 1][centerIndex].getTerrain() == Terrain.NONE
				|| scanMapTiles[centerIndex + 1][centerIndex].getTerrain() == Terrain.FLUID
				|| scanMapTiles[centerIndex + 1][centerIndex].getTerrain() == Terrain.SAND) {
			return true;
		}

		return false;
	}

	private void setCurrentLoc() throws IOException {

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
	}

	private Coord returnCurrentLoc() throws IOException {

		String line;
		Coord clone = new Coord(-1, -1);
		// **** Request Rover Location from SwarmServer ****
		out.println("LOC");
		line = in.readLine();
		if (line == null) {
			System.out.println(rovername + " check connection to server");
			line = "";
		}
		if (line.startsWith("LOC")) {
			// loc = line.substring(4);
			clone = extractLocationFromString(line);
		}
		return clone;
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
	// array group12 - this raw JsonData should be used for our maptileLog?
	public void loadScanMapFromSwarmServer() throws IOException {
		// System.out.println("ROVER_12 method doScan()");
		setCurrentLoc();
		Coord scanLoc = new Coord(currentLoc.getXpos(), currentLoc.getYpos());
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

		System.out.println("+++++++++++++++ jsonScanMapString +++++++++++++++");
		System.out.println(jsonScanMapString.toString());

		// System.out.println("ROVER_12 convert from json back to ScanMap class");
		// convert from the json string back to a ScanMap object
		scanMap = gson.fromJson(jsonScanMapString, ScanMap.class);

		myJSONStringBackupofMap = jsonScanMapString;
		loadMapTilesOntoGlobalMapLog(scanMap.getScanMap(), scanLoc);
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

	private int requestTimeRemaining(Socket soc) throws IOException {

		// **** Request Remaining Time from SwarmServer ****
		out.println("TIMER");
		line = in.readLine();
		int timeRemaining = -2;
		if (line == null) {
			System.out.println(rovername + " check connection to server");
			line = "";
		}
		if (line.startsWith("TIMER")) {
			timeRemaining = extractTimeRemaining(line);

		}
		return timeRemaining;
	}

	private static Coord extractCurrLOC(String sStr) {
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

	private static Coord extractStartLOC(String sStr) {

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

	private static Coord extractTargetLOC(String sStr) {
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

	private static int extractTimeRemaining(String sStr) {
		sStr = sStr.substring(6);
		if (sStr.lastIndexOf(" ") != -1) {
			String timeStr = sStr.substring(0, sStr.lastIndexOf(" "));
			return Integer.parseInt(timeStr);
		}
		return -1;
	}

	// this takes the server response string, parses out the x and x values and
	// returns a Coord object
	private static Coord extractLocationFromString(String sStr) {
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

	public void debugPrintMapTileArrayText(Map<Coord, MapTile> globalMapCopy,
			int mapSize) {
		MapTile tile;

		for (int y = 0; y < mapSize; y++) {
			for (int x = 0; x < mapSize; x++) {
				tile = globalMapCopy.get(new Coord(x, y));
				System.out.print("x,y=" + x + "," + y + "\t" + tile + "\t/t");
			}
		}
	}

	public void debugPrintMapTileArray(Map<Coord, MapTile> globalMapCopy) {

		// FIXME
		int edgeSize = 100;
		System.out.println("edge size: " + edgeSize);
		for (int k = 0; k < edgeSize + 2; k++) {
			System.out.print("--");
		}

		System.out.print("\n");

		for (int j = 0; j < edgeSize; j++) {

			// System.out.print("j=" + j + "\t");

			System.out.print("| ");
			for (int i = 0; i < edgeSize; i++) {
				if (mapTileLog.get(new Coord(i, j)) == null) {
					System.out.print("nn");
				}
				// check and print edge of map has first priority
				else if (mapTileLog.get(new Coord(i, j)).getTerrain()
						.toString().equals("NONE")) {
					System.out.print("XX");

					// next most important - print terrain and/or science
					// locations
					// terrain and science
				} else if (!(mapTileLog.get(new Coord(i, j)).getTerrain()
						.toString().equals("SOIL"))
						&& !(mapTileLog.get(new Coord(i, j)).getScience()
								.toString().equals("NONE"))) {
					// both terrain and science

					System.out.print(mapTileLog.get(new Coord(i, j))
							.getTerrain().toString().substring(0, 1)
							+ mapTileLog.get(new Coord(i, j)).getScience()
									.getSciString());
					// just terrain
				} else if (!(mapTileLog.get(new Coord(i, j)).getTerrain()
						.toString().equals("SOIL"))) {
					System.out.print(mapTileLog.get(new Coord(i, j))
							.getTerrain().toString().substring(0, 1)
							+ " ");
					// just science
				} else if (!(mapTileLog.get(new Coord(i, j)).getScience()
						.toString().equals("NONE"))) {
					System.out.print(" "
							+ mapTileLog.get(new Coord(i, j)).getScience()
									.getSciString());

					// if still empty check for rovers and print them
				} else if (mapTileLog.get(new Coord(i, j)).getHasRover()) {
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

	private void loadMapTilesOntoGlobalMapLog(MapTile[][] ptrScanMap,
			Coord scanLoc) {

		MapTile tempTile;
		Coord tempCoord;
		Terrain ter;
		Science sci;
		int elev;
		boolean hasR;
		int halfTileSize = ptrScanMap.length / 2;

		// debug - print out
		System.out.println("inside of loadMapTileIntoGlobal()[scanLoc="

		+ scanLoc + "]:" + "[currLoc=" + currentLoc);

		System.out.println("ptrScanMap Size: " + ptrScanMap.length);

		for (int y = 0; y < ptrScanMap.length; y++) {
			for (int x = 0; x < ptrScanMap.length; x++) {

				ter = ptrScanMap[x][y].getTerrain();
				sci = ptrScanMap[x][y].getScience();
				elev = ptrScanMap[x][y].getElevation();
				hasR = ptrScanMap[x][y].getHasRover();

				tempTile = new MapTile(ter, sci, elev, hasR);
				tempCoord = new Coord((scanLoc.getXpos() - halfTileSize) + x,
						scanLoc.getYpos() - halfTileSize + y);

				// debug
				System.out.println("(x,y)=(" + x + "," + y + ")|" + "(X,Y)=("
						+ (scanLoc.getXpos() - halfTileSize + x) + ","
						+ (scanLoc.getYpos() - halfTileSize + y) + ")\t"
						+ tempCoord + tempTile);
				// our copy of the scanned map in global context
				mapTileLog.put(tempCoord, tempTile);

				// Create JSON object
				JSONObject obj = new JSONObject();
				obj.put("x", new Integer(tempCoord.getXpos()));
				obj.put("y", new Integer(tempCoord.getYpos()));

				// Check if terrain exist
				if (!ter.getTerString().isEmpty()) {
					obj.put("terrain", new String(ter.getTerString()));
				} else {
					obj.put("terrain", new String(""));
				}
				// Check if science exist
				if (!sci.getSciString().isEmpty()) {
					obj.put("science", new String(sci.getSciString()));
					obj.put("stillExists", new Boolean(true));
				} else {
					obj.put("science", new String(""));
					obj.put("stillExists", new Boolean(false));
				}
				try {
					 sendPost(obj);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// Send JSON object to server using HTTP POST method
				// sendJSONToServer(obj, "http://192.168.0.101:3000/globalMap");
				// sendJSONToServer(obj,
				// "http://192.168.0.101:3000/scout");https:
				// sendJSONToServer(obj,
				// "www.reddit.com/r/explainlikeimfive/comments/4ibqm3.json");
				// -----------------------------------

			}
		}
	}

	private void sendJSONToServer(JSONObject obj, String URL) {
		// TODO need testing
		try {
			HttpClient client = HttpClientBuilder.create().build();
			HttpPost post = new HttpPost(URL);

			StringEntity se = new StringEntity(obj.toString());
			post.setHeader("content-type", "application/json");
			post.setEntity(se);

			HttpResponse response = client.execute(post);

			// Check response
			System.out.println(obj.toString());

			System.out.println("Response Code : "
					+ response.getStatusLine().getStatusCode());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// HTTP POST request
		private void sendPost(JSONObject jsonObj) throws Exception {
			String url = "http://localhost:3000/scout"; //<-- Wed. 5/11
			//String url = "http://192.168.0.101:3000/scout"; <-- Sat 5/7/16
			// String url = "https://selfsolve.apple.com/wcResults.do";
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			String USER_AGENT = "ROVER 12";
			// add reuqest header
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", USER_AGENT);
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			con.setDoOutput(true);

			// Send post request
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(jsonObj.toString());// this throws an exception?
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'POST' request to URL : " + url);
			System.out.println("Post parameters : " + jsonObj.toString());
			System.out.println("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// print result
			System.out.println(response.toString());

		}

	
	private String request(MapTile[][] scanMapTile) {

		String USER_AGENT = "ROVER_11";
		String url = "http://192.168.0.101:3000/globalMap";

		URL obj = null;

		String responseStr = "";
		try {
			obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			con.setRequestMethod("GET");

			// add request header
			con.setRequestProperty("User-Agent", USER_AGENT);

			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'GET' request to URL : " + url);
			System.out.println("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// print result
			responseStr = response.toString();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// optional default is GET

		return responseStr;
	}

	private void move(String dir) throws IOException {
		System.out.println("current location in move(): " + currentLoc);
		setCurrentLoc();
		// doScanOriginal();

		switch (dir) {

		case "E":
			if (!isSand("E")) {
				System.out.println("request move -> E");
				moveEast();
			}
			break;
		case "W":
			if (!isSand("W")) {
				System.out.println("request move -> W");
				moveWest();
			}
			break;
		case "N":
			if (!isSand("N")) {
				System.out.println("request move -> N");
				moveNorth();
			}
			break;
		case "S":
			if (!isSand("S")) {
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

	public boolean isSand(String direction) {

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

	// a check function to prevent IndexOutOfBounds exception
	public boolean withinTheGrid(int i, int j, int arrayLength) {
		return i >= 0 && j >= 0 && i < arrayLength && j < arrayLength;
	}

	// KS - must complete
	public boolean isObstacle(String direction) {

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

			// Checks whether there is sand or rock in the next tile
			if (scanMap.getScanMap()[x][y].getTerrain() == Terrain.SAND
					|| scanMap.getScanMap()[x][y].getTerrain() == Terrain.ROCK) {
				return true;
			}
			return false;
		}
		return false;
	}


	
	
	private int[] approximateGlobalMapDimension(){
		return null;
	}
	
	// KS - must complete
	private Coord getG12Target() {
		// 1. divide the map into quadrants
		// 2. count num of null cells
		// 3. take the quadrant with the most null cell, and repeat until the
		// target quadrant is a single cell

		return new Coord(-1, -1);
	}
	
	/*
	 * Finds the index/indices of the element(s) of an array that has the largest value, puts the index into a set, returns the set.
	 * Indices of all duplicate elements will be accounted.  
	 */
	private Set<Integer> findMaxIndeces(int[] array) {
		
		int max = Integer.MIN_VALUE, maxIndex = -1;
		Set<Integer> maxIndices = new HashSet<Integer>();
		for (int i = 0; i < array.length; i++) {
			if (max < array[i]) {
				maxIndex = i;
				max = array[i];
			}
		}
		maxIndices.add(maxIndex);
		/*
		 * if 2 or more quadrant ties, return the farthest from current location
		 * of rover 12
		 */
		for (int i = 0; i < array.length; i++) {
			if (max == array[i]) {
				maxIndices.add(i);
			}
		}
		return maxIndices;
	}

	private double getDistanceBetween2Points(Coord p1, Coord p2) {
		return Math.sqrt(Math.pow(p2.getXpos() - p1.getXpos(), 2)
				+ Math.pow(p2.getYpos() - p1.getYpos(), 2));
	}

	private int getFurthestQuadrant(Coord q1, Coord q2, Coord q3, Coord q4) {

		double[] distances = { 0, getDistanceBetween2Points(q1, currentLoc),
				getDistanceBetween2Points(q2, currentLoc),
				getDistanceBetween2Points(q3, currentLoc),
				getDistanceBetween2Points(q4, currentLoc) };

		double max = Double.MIN_VALUE;
		int maxIndex = -1;
		for (int i = 0; i < distances.length; i++) {
			if (max < distances[i]) {
				maxIndex = i;
				max = distances[i];
			}
		}

		return maxIndex;
	}

	private Coord getRover12TargetAreas() {

		// approximate the size of the map
		int w = targetLocation.getXpos(), h = targetLocation.getYpos(), q1 = 0, q2 = 0, q3 = 0, q4 = 0;
		Coord target_rv12 = new Coord(-1, -1);

		// quadrant I
		for (int j = 0; j < h / 2; j++) {
			for (int i = 0; i < w / 2; i++) {
				if (mapTileLog.get(new Coord(i, j)) == null) {
					q1++;
				}
			}
		}

		// quadrant II
		for (int j = 0; j < h / 2; j++) {
			for (int i = w / 2 + 1; i < w; i++) {
				if (mapTileLog.get(new Coord(i, j)) == null) {
					q2++;
				}
			}
		}

		// quadrant III
		for (int j = h / 2; j < h; j++) {
			for (int i = 0; i < w / 2; i++) {
				if (mapTileLog.get(new Coord(i, j)) == null) {
					q3++;
				}
			}
		}

		// quadrant IV
		for (int j = h / 2; j < h; j++) {
			for (int i = w / 2 + 1; i < w; i++) {
				if (mapTileLog.get(new Coord(i, j)) == null) {
					q4++;
				}
			}
		}
		int[] array = { 0, q1, q2, q3, q4 };
		Set<Integer> maxIndeces = findMaxIndeces(array);

		// if there are ties, get the furthest quadrant
		if (maxIndeces.size() > 1) {

		}
		Random rd = new Random();
		int num = rd.nextInt(array.length);
		Object[] tempArray = findMaxIndeces(array).toArray();
		int maxIdx = (Integer) tempArray[num];

		switch (maxIdx) {
		case 1: // Quadrant I
			return new Coord(w / 4, h / 4);
		case 2: // Quadrant II
			return new Coord((w * 3 / 4), h / 4);
		case 3: // Quadrant III
			return new Coord(w / 4, h * 3 / 4);
		case 4: // Quadrant IV
			return new Coord(w * 3 / 4, h * 3 / 4);
		default:
			break;
		}
		return target_rv12;
	}

	/**
	 * Runs the client
	 */
	public static void main(String[] args) throws Exception {
		ROVER_12_wk7_kae client = new ROVER_12_wk7_kae();
		client.run();
	}
}