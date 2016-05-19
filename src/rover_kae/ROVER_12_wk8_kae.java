package rover_kae;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import common.Communication;
import common.Coord;
import common.MapTile;
import common.ScanMap;
import enums.Science;
import enums.Terrain;
import supportTools.CommunicationUtil;
import supportTools.Path;
import supportTools.RoverMotionUtil;

/**
 * The seed that this program is built on is a chat program example found here:
 * http://cs.lmu.edu/~ray/notes/javanetexamples/ Many thanks to the authors for
 * publishing their code examples
 */

public class ROVER_12_wk8_kae {

	private BufferedReader in;
	private PrintWriter out;
	private String rovername;
	private ScanMap scanMap;
	private int sleepTime;
	private String SERVER_ADDRESS = "localhost", line;
	static final int PORT_ADDRESS = 9537;

	// Group 12 variables
	static String myJSONStringBackupofMap;
	private Coord currentLoc, previousLoc, rovergroupStartPosition = null,
			targetLocation = null;

	private Map<Coord, MapTile> mapTileLog = new HashMap<Coord, MapTile>();
	//private Set<Coord,Path> visitCounts = new HashSet<Coord,Path>();// manage this only
														// after targetLoc has
														// been
	// visited
	// private Map<Coord, Path> pathMap = new HashMap<Coord, Path>();
	// private Deque<Coord> pathStack = new ArrayDeque<Coord>();
	public ArrayList<Coord> pathMap = new ArrayList<Coord>();
	public Map<Coord, Path> pathMapUltra = new HashMap<Coord,Path>();
	
	private Random rd = new Random();
	private boolean[] cardinals = new boolean[4];
	private boolean isTargetLocReached = false;
	private Coord nextTarget;

	public ROVER_12_wk8_kae() {
		// constructor
		System.out.println("ROVER_12 rover object constructed");
		rovername = "ROVER_12";
		SERVER_ADDRESS = "localhost";
		// this should be a safe but slow timer value
		sleepTime = 500; // in milliseconds - smaller is faster, but the server
							// will cut connection if it is too small
	}

	public ROVER_12_wk8_kae(String serverAddress) {
		// constructor
		System.out.println("ROVER_12 rover object constructed");
		rovername = "ROVER_12";
		SERVER_ADDRESS = serverAddress;
		sleepTime = 500; // in milliseconds - smaller is faster, but the server
							// will cut connection if it is too small
	}

	private void run() throws IOException, InterruptedException {

		// Make connection to GreenCorp Server
		// String url = "http://23.251.155.186:3000/api/global";
		// Communication com = new Communication(url, rovername, corp_secret);

		// if(targetReached)

		ArrayList<String> equipment = new ArrayList<String>();
		boolean beenToTargetLoc = false;
		Socket socket = null;

		try {

			// initialize variables
			socket = connectToSwarmServer();
			equipment = getEquipment();
			rovergroupStartPosition = requestStartLoc(socket);
			targetLocation = requestTargetLoc(socket);
			nextTarget = targetLocation.clone();

			/**
			 * #### Rover controller process loop ####
			 */
			boolean firstItr = true;
			Coord prevLoc = currentLoc.clone();
			cardinals[1] = true;

			while (true) {

				// prevLoc = currentLoc.clone();
				setCurrentLoc(); // BEFORE the move() in this iteration
				pathMap.add(new Coord(currentLoc.xpos, currentLoc.ypos));
				System.out.println("BEFORE: " + currentLoc);
				int numSteps = pathMap.size();

				// ***** do a SCAN ******
				// if (numSteps % 5 == 0 || numSteps == 1) {
				loadScanMapFromSwarmServer();
				// debug
				// scanMap.debugPrintMap();
				debugPrintMapTileArray(mapTileLog);
				// }

				MapTile[][] scanMapTiles = scanMap.getScanMap();
				// com.postScanMapTiles(currentLoc, scanMapTiles);

				// random();
				// roverMotionLogic(cardinals, scanMapTiles, currentLoc.xpos,
				// currentLoc.ypos);

				sinusoidal_East(targetLocation);

				setCurrentLoc(); // AFTER this iteration
				System.out.println("AFTER: " + currentLoc);

				System.out
						.println("ROVER_12 ------------ bottom process control --------------");
				// Thread.sleep(sleepTime);
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
	}// END of run()

	/**
	 * Connects to the server then enters the processing loop.
	 */
	private void run_ks() throws IOException, InterruptedException {

		// Make connection to GreenCorp Server
		// String url = "http://23.251.155.186:3000/api/global";
		// Communication com = new Communication(url, rovername, corp_secret);

		boolean beenToTargetLoc = false;
		Socket socket = null;
		try {

			// Make connection to SwarmServer and initialize streams
			socket = connectToSwarmServer();

			ArrayList<String> equipment = new ArrayList<String>();
			equipment = getEquipment();
			rovergroupStartPosition = requestStartLoc(socket);
			targetLocation = requestTargetLoc(socket);

			/**
			 * #### Rover controller process loop ####
			 */
			boolean firstItr = true;
			Coord prevLoc = currentLoc.clone();

			while (true) {

				prevLoc = currentLoc.clone();
				setCurrentLoc(); // BEFORE the move() in this iteration

				System.out.println("BEFORE: " + currentLoc);
				int numSteps = pathMap.size();

				// ***** do a SCAN ******
				if (numSteps % 5 == 0) {
					loadScanMapFromSwarmServer();
					// debug
					// scanMap.debugPrintMap();
					// debugPrintMapTileArray(mapTileLog);
				}

				// ******* store rover 12 path for easy return
				pathMap.add(new Coord(currentLoc.xpos, currentLoc.ypos));
				// pathMap.put(currentLoc.clone(), new
				// Path(currentLoc.clone(),prevLoc.clone()));
				// ******* pathStack.add(currentLoc.clone());

				MapTile[][] scanMapTiles = scanMap.getScanMap();
				// com.postScanMapTiles(currentLoc, scanMapTiles);

				if (firstItr) {
					move("E");
					System.out.println("step to E 1");
					Thread.sleep(700);
					move("E");
					System.out.println("step to E 2");
					Thread.sleep(700);
					move("E");
					System.out.println("step to E 3");
					Thread.sleep(700);
					move("E");
					System.out.println("step to E 3");
					Thread.sleep(550);
					firstItr = false;
				}

				// ***** MOVING *****
				int centerIndex = (scanMap.getEdgeSize() - 1) / 2;

				// moveRover();
				move("S");
				setCurrentLoc(); // AFTER this iteration
				System.out.println("AFTER: " + currentLoc);
				Thread.sleep(sleepTime); // G12 - sleepTime has been reduced to
											// 100. is that alright?

				// if(currentLoc.equals(targetLocation) && ){
				// beenToTargetLoc = true;
				// }

				System.out
						.println("ROVER_12 ------------ bottom process control --------------");
				int idx = 0;
				for (Coord pt : pathMap) {
					System.out.println("i=" + (idx++) + "\t" + pt);
				}
				// Thread.sleep(5000);

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
	}// END of run_ks()

	private Socket connectToSwarmServer() throws UnknownHostException,
			IOException {
		Socket socket;
		socket = new Socket(SERVER_ADDRESS, PORT_ADDRESS);

		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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
		return socket;
	}

	private void moveRover() throws IOException {
		String dir = chooseDir();
		move(dir);
	}

	private String chooseDir() throws IOException {

		// if (isPastPositonIsEast()) {
		// move("E");
		//
		// } else if (isPastPositonIsWest() {
		// move("W");
		//
		// } else if (isPastPositonIsNorth(cardinals, eachCoord, currentXPos,
		// currentYPos)) {
		// move("N");
		//
		// } else if (isPastPositonIsSouth(cardinals, eachCoord, currentXPos,
		// currentYPos)) {
		// move("S");
		// }
		return "huh?";
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

	private boolean isTowardsEastIsObsatacle(MapTile[][] scanMapTiles,
			int centerIndex, int i) {
		// i = # steps away
		if (scanMapTiles[centerIndex + 1][centerIndex].getHasRover()
				|| scanMapTiles[centerIndex + i][centerIndex].getTerrain() == Terrain.ROCK
				|| scanMapTiles[centerIndex + i][centerIndex].getTerrain() == Terrain.NONE
				|| scanMapTiles[centerIndex + i][centerIndex].getTerrain() == Terrain.FLUID
				|| scanMapTiles[centerIndex + i][centerIndex].getTerrain() == Terrain.SAND) {
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

	private Coord requestCurrentLoc() throws IOException {

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
		Coord scanLoc = new Coord(currentLoc.xpos, currentLoc.ypos);
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

		scanMap = gson.fromJson(jsonScanMapString, ScanMap.class);

		// myJSONStringBackupofMap = jsonScanMapString;
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

		out.println("START_LOC " + currentLoc.xpos + " " + currentLoc.ypos);
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

		out.println("TARGET_LOC " + currentLoc.xpos + " " + currentLoc.ypos);
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

	// Our hats off to brilliant ROVER_11 / Group 11! Many thanks!
	private void updateFromGreenCorpGlobalMap(JSONArray data) {

		for (Object o : data) {

			JSONObject jsonObj = (JSONObject) o;
			int x = (int) (long) jsonObj.get("x");
			int y = (int) (long) jsonObj.get("y");
			Coord coord = new Coord(x, y);

			if (!mapTileLog.containsKey(coord)) {
				MapTile tile = supportTools.CommunicationUtil
						.convertToMapTile(jsonObj);

				mapTileLog.put(coord, tile);
			}
		}
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

	public static int extractTimeRemaining(String sStr) {
		sStr = sStr.substring(6);
		if (sStr.lastIndexOf(" ") != -1) {
			String timeStr = sStr.substring(0, sStr.lastIndexOf(" "));
			return Integer.parseInt(timeStr);
		}
		return -1;
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

	private void recordPath() {

	}

	private void loadMapTilesOntoGlobalMapLog(MapTile[][] ptrScanMap,
			Coord scanLoc) {

		MapTile tempTile;
		Coord tempCoord;
		Terrain ter;
		Science sci;
		int elev;
		boolean hasR;
		int halfScanMapSize = ptrScanMap.length / 2;

		for (int y = 0; y < ptrScanMap.length; y++) {
			for (int x = 0; x < ptrScanMap.length; x++) {

				tempCoord = new Coord((scanLoc.xpos - halfScanMapSize) + x,
						scanLoc.ypos - halfScanMapSize + y);

				if (!mapTileLog.containsKey(tempCoord)) {
					ter = ptrScanMap[x][y].getTerrain();
					sci = ptrScanMap[x][y].getScience();
					elev = ptrScanMap[x][y].getElevation();
					hasR = ptrScanMap[x][y].getHasRover();

					tempTile = new MapTile(ter, sci, elev, hasR);
					mapTileLog.put(tempCoord, tempTile);
				}
			}
		}
	}

	// HTTP POST request
	public void sendPost(JSONObject jsonObj) throws Exception {

		String url = "http://23.251.155.186:3000/api";
		String corp_secret = "0FSj7Pn23t";

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
		wr.writeBytes(jsonObj.toString());
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

	private void move(String dir) throws IOException {

		switch (dir) {

		case "E":
			if (!isSandOrRock("E")) {
				// System.out.println("request move -> E"); debug
				moveEast();
			}
			break;
		case "W":
			if (!isSandOrRock("W")) {
				// System.out.println("request move -> W");
				moveWest();
			}
			break;
		case "N":
			if (!isSandOrRock("N")) {
				// System.out.println("request move -> N");
				moveNorth();
			}
			break;
		case "S":
			if (!isSandOrRock("S")) {
				// System.out.println("request move -> S");
				moveSouth();
			}
			break;
		default:
			break;
		}
	}

	private void moveWest() {
		out.println("MOVE W");
		System.out.println("ROVER_12 request move W");
		cardinals[0] = false; // S
		cardinals[1] = false; // E
		cardinals[2] = false; // N
		cardinals[3] = true; // W
	}

	private void moveNorth() {
		out.println("MOVE N");
		System.out.println("ROVER_12 request move N");
		cardinals[0] = false; // S
		cardinals[1] = false; // E
		cardinals[2] = true; // N
		cardinals[3] = false; // W
	}

	private void moveSouth() {
		out.println("MOVE S");
		System.out.println("ROVER_12 request move S");
		cardinals[0] = true; // S
		cardinals[1] = false; // E
		cardinals[2] = false; // N
		cardinals[3] = false; // W
	}

	private void moveEast() {
		out.println("MOVE E");
		System.out.println("ROVER_12 request move E");
		cardinals[0] = false; // S
		cardinals[1] = true; // E
		cardinals[2] = false; // N
		cardinals[3] = false; // W
	}

	public boolean isSandOrRock(String direction) {

		int x = currentLoc.xpos, y = currentLoc.ypos;

		if (direction == "S")
			y += 1;
		else if (direction == "N")
			y -= 1;
		else if (direction == "E")
			x += 1;
		else
			// "W"
			x -= 1;

		if (!mapTileLog.containsKey(new Coord(x, y))) {
			try {
			// if mapTileLog does not contain the target info, just load it.
				loadScanMapFromSwarmServer();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Checks whether there is sand in the next tile
		if (mapTileLog.get(new Coord(x, y)).getTerrain() == Terrain.SAND
				|| mapTileLog.get(new Coord(x, y)).getTerrain() == Terrain.ROCK
				|| mapTileLog.get(new Coord(x, y)).getTerrain() == Terrain.NONE
				|| mapTileLog.get(new Coord(x, y)).getTerrain() == Terrain.FLUID)
			return true;

		return false;
	}

	// a check function to prevent IndexOutOfBounds exception
	public boolean withinTheGrid(int i, int j, int arrayLength) {
		return i >= 0 && j >= 0 && i < arrayLength && j < arrayLength;
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

	public int randomNum(int min, int max) {
		return rd.nextInt(max + 1) + min;
	}

	// take a random step (just one step) to break the pattern
	private void random() throws InterruptedException, IOException {

		String[] directions = { "N", "E", "S", "W" };

		while (isStuck()) {
			move(directions[randomNum(0, 3)]);
			setCurrentLoc();
			pathMap.add(currentLoc.clone());
			Thread.sleep(sleepTime);
		}
	}

	public int countUnvisited(Coord currLoc, int searchSize) {
		// searchSize should be an even number
		int numUnvisited = 0;

		for (int j = currLoc.ypos - searchSize / 2; j < currLoc.ypos
				+ searchSize / 2; j++) {
			for (int i = currLoc.xpos - searchSize / 2; i < currLoc.ypos
					+ searchSize / 2; i++) {
				if (!mapTileLog.containsKey(new Coord(i, j))) {
					numUnvisited++;
				}
			}
		}
		return numUnvisited;
	}

	public boolean visited(Coord pos) {
		if (mapTileLog.containsKey(pos)) {
			return true;
		}
		return false;
	}

	public Coord getNextTargetCoord() {

		boolean isTargetLocReached = !mapTileLog.containsKey(targetLocation);
		int searchSize = 30, nullCounter = 0;
		// Coord nextTarget= new Coord(randomNum(min, max));

		if (!visited(targetLocation)) {
			return targetLocation;
		}

		// while()
		/*if (visitCounts.size() < 1) {

		}*/
		return null;
	}

	public boolean isGoingInCircle() {
		
		int searchSize = 10;
		//
		int numUnVisited = countUnvisited(currentLoc, searchSize);
		if (numUnVisited <= 0) {
			System.out.println("we are going in circle!");// debug
			return true;
		} else
			return false;
	}

	// return true if rover is in the same position after 3 controll move
	// iterations
	public boolean isStuck() {

		int stuckThreshHold = 6, last = pathMap.size() - 1;

		if (pathMap.size() < stuckThreshHold) {
			return false;
		}

		for (int i = 0; i < stuckThreshHold; i++) {
			if (pathMap.get(last).equals(pathMap.get(last - i))) {
				return false;
			}
		}
		// KS - need refinements

		// debug
		System.out.println("we are stuck!");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	private void sinusoidal_East(Coord target) throws InterruptedException,
			IOException {

		int waveLength = 5, waveHeight = 3, steps = waveLength;
		String[] directions = { "E", "S", "E", "N" };
		boolean targetReached = false;
		String currentDir;

		if (currentLoc.equals(target)) {
			return;
		}

		while (!targetReached && !isStuck()
				&& currentLoc.xpos < targetLocation.xpos) {

			if (currentLoc.xpos > targetLocation.xpos) {
				sinusoidal_West(target);
			}

			//if(goingInCircle){}
			
			// jump out of the loop if eastern edge of the land is reached
			for (int i = 0; i < cardinals.length; i++) {

				currentDir = directions[i];
				if (currentDir.equals("E")) {
					steps = waveLength;
				} else {
					steps = waveHeight;
				}

				for (int j = 0; j < steps; j++) {
					move(currentDir);
					Thread.sleep(500);
				}
			}

			setCurrentLoc();
			pathMap.add(currentLoc.clone());
			if (currentLoc.equals(target)) {
				targetReached = true;
			}
		}

	}

	private void sinusoidal_West(Coord target) throws InterruptedException,
			IOException {

		int waveLength = 5, waveHeight = 3, steps = waveLength;
		String[] directions = { "W", "S", "W", "N" };
		boolean targetReached = false;
		String currentDir;

		if (currentLoc.equals(target)) {
			return;
		}

		if (currentLoc.xpos > targetLocation.xpos) {
			// sinusoidal_west();
		}

		while (targetReached && !isStuck()
				&& currentLoc.xpos < targetLocation.xpos) {
			// jump out of the loop if eastern edge of the land is reached
			for (int i = 0; i < cardinals.length; i++) {

				currentDir = directions[i];
				if (currentDir.equals("E")) {
					steps = waveLength;
				} else {
					steps = waveHeight;
				}

				for (int j = 0; j < steps; j++) {
					move(currentDir);
					Thread.sleep(700);
				}
			}

			setCurrentLoc();
			pathMap.add(currentLoc.clone());
			if (currentLoc.equals(target)) {
				targetReached = true;
			}
		}

	}

	// private void sinusoidal(Coord target, String dir)
	// throws InterruptedException, IOException {
	//
	// int longer = 5, shorter = 3, waveLength = longer, waveHeight = shorter,
	// steps = waveLength;
	// String[] directions = {"E","S","E","N"};
	// switch (dir) {
	// case "N":
	// directions[0]="N";
	// directions[1]="E";
	// directions[2]="N";
	// directions[3]="W";
	// break;
	// case "E":
	// directions[0]="E";
	// directions[1]="S";
	// directions[2]="E";
	// directions[3]="N";
	// break;
	// case "S":
	// directions[0]="S";
	// directions[1]="E";
	// directions[2]="S";
	// directions[3]="W";
	// break;
	// case "W":
	// directions[0]="W";
	// directions[1]="S";
	// directions[2]="W";
	// directions[3]="N";
	// break;
	//
	// default:
	// break;
	// }
	//
	// boolean targetReached = false;
	// String currentDir;
	//
	// if (currentLoc.equals(target)) {
	// return;
	// }
	//
	// if (currentLoc.xpos > targetLocation.xpos) {
	// // sinusoidal_west();
	// }
	//
	// while (targetReached && !isStuck()
	// && currentLoc.xpos < targetLocation.xpos) {
	// // jump out of the loop if eastern edge of the land is reached
	// for (int i = 0; i < cardinals.length; i++) {
	//
	// currentDir = directions[i];
	// if (currentDir.equals("E")) {
	// steps = waveLength;
	// } else {
	// steps = waveHeight;
	// }
	//
	// for (int j = 0; j < steps; j++) {
	// move(currentDir);
	// Thread.sleep(700);
	// }
	// }
	//
	// setCurrentLoc();
	// pathMap.add(currentLoc.clone());
	// if (currentLoc.equals(target)) {
	// targetReached = true;
	// }
	// }
	// }

	private void 
Logic(boolean[] cardinals,
			MapTile[][] scanMapTiles, int currentXPos, int currentYPos)
			throws InterruptedException, IOException {

		if (isStuck()) {
			random();
		}

		// ************* Febi's rover motion logic **********
		int centerIndex = (scanMap.getEdgeSize() - 1) / 2;

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
				move("E");

			} else if (scanMapTiles[centerIndex][centerIndex + 1].getScience()
					.equals("C")) {
				move("S");

			} else if (scanMapTiles[centerIndex][centerIndex - 1].getScience()
					.equals("C")) {
				move("N");
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
								move("E");
							} else {
								cardinals = randomPickMotion(cardinals,
										centerIndex, scanMapTiles);
								// cardinals = moveUsingPastPath(cardinals,
								// currentXPos, currentYPos);
							}

						} else {
							move("N");
						}
					} else {
						move("S");
					}
				}
				// when no obstacle is in next move to east
				else {
					move("E");
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
							move("W");
						} else {
							cardinals = randomPickMotion(cardinals,
									centerIndex, scanMapTiles);
						}

					} else {
						move("N");
					}
				} else {
					move("S");
				}
			}
			// when no obstacle is in next move to west
			else {
				move("W");
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
							move("S");
						} else {
							cardinals = randomPickMotion(cardinals,
									centerIndex, scanMapTiles);
						}
					} else {
						move("E");
					}
				} else {
					move("W");
				}
			}
			// when no obstacle is in next move to south
			else {
				move("S");
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
						if (isTowardsSouthIsObsatacle(scanMapTiles, centerIndex)) {
							move("N");
						} else {
							cardinals = randomPickMotion(cardinals,
									centerIndex, scanMapTiles);

							// cardinals = moveUsingPastPath(cardinals,
							// currentXPos, currentYPos);
						}
					} else {
						move("E");
					}
				} else {
					move("W");
				}
			}
			// when no obstacle is in next move to north
			else {
				move("N");
			}
		}
	}

	private boolean[] randomPickMotion(boolean[] cardinals, int centerIndex,
			MapTile[][] scanMapTiles) {
		int randomNumber = randomNum(0, 3);
		try {
			if (cardinals[randomNumber] == true) {
				randomPickMotion(cardinals, centerIndex, scanMapTiles);
			} else {
				switch (randomNumber) {

				// going south
				case 0: {
					if (isTowardsSouthIsObsatacle(scanMapTiles, centerIndex)) {
						randomPickMotion(cardinals, centerIndex, scanMapTiles);
					} else {

						move("S");

					}
					break;
				}

				// going east
				case 1: {
					if (isTowardsEastIsObsatacle(scanMapTiles, centerIndex)) {
						randomPickMotion(cardinals, centerIndex, scanMapTiles);
					} else {
						move("E");
					}
					break;
				}

				// going north
				case 2: {

					if (isTowardsNorthIsObsatacle(scanMapTiles, centerIndex)) {
						randomPickMotion(cardinals, centerIndex, scanMapTiles);
					} else {
						move("N");
					}
					break;
				}

				// going west

				case 3: {
					if (isTowardsSouthIsObsatacle(scanMapTiles, centerIndex)) {
						randomPickMotion(cardinals, centerIndex, scanMapTiles);
					} else {
						move("S");
					}
					break;
				}

				}

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cardinals;
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

	/**
	 * Runs the client
	 */
	public static void main(String[] args) throws Exception {
		ROVER_12_wk8_kae client = new ROVER_12_wk8_kae();
		client.run();
	}
}