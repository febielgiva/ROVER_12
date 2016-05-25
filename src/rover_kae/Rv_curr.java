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
import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;

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

public class Rv_curr {

	BufferedReader in;
	PrintWriter out;
	String rovername;
	ScanMap scanMap;
	int sleepTime;
	String SERVER_ADDRESS = "localhost", line;
	// String SERVER_ADDRESS = "192.168.1.106", line;
	static final int PORT_ADDRESS = 9537;

	// Group 12 variables
	int numLogics = 3;
	static String myJSONStringBackupofMap;
	Coord currentLoc, previousLoc, rovergroupStartPosition = null,
			targetLocation = null;

	Map<Coord, MapTile> mapTileLog = new HashMap<Coord, MapTile>();
	HashMap<Coord, Path> visitCounts = new HashMap<Coord, Path>();// manage
																	// this
																	// only
	// after targetLoc has
	// been
	// visited
	// Map<Coord, Path> pathMap = new HashMap<Coord, Path>();
	// Deque<Coord> pathStack = new ArrayDeque<Coord>();
	public ArrayList<Coord> pathMap = new ArrayList<Coord>();

	Random rd = new Random();
	boolean[] cardinals = new boolean[4];
	boolean isTargetLocReached = false;
	Coord nextTarget;

	public Rv_curr() {
		// constructor
		System.out.println("ROVER_12 rover object constructed");
		rovername = "ROVER_12";
		// SERVER_ADDRESS = "192.168.1.106";
		SERVER_ADDRESS = "localhost";
		// this should be a safe but slow timer value
		sleepTime = 500; // in milliseconds - smaller is faster, but the server
							// will cut connection if it is too small
	}

	public Rv_curr(String serverAddress) {
		// constructor
		System.out.println("ROVER_12 rover object constructed");
		rovername = "ROVER_12";
		SERVER_ADDRESS = serverAddress;
		sleepTime = 500; // in milliseconds - smaller is faster, but the server
							// will cut connection if it is too small
	}

	void roverMotionLogic(boolean[] cardinals, MapTile[][] scanMapTiles,
			int centerIndex, int currentXPos, int currentYPos)
			throws InterruptedException, IOException {
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
						// move south if no obstacle or else go back to
						// north098uuu
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

	void followRhsWall(MapTile[][] scanMapTiles, int centerIndex)
			throws IOException {

		String[] directions = { "S", "E", "N", "W" };
		switch (getFacingDirection()) {
		case "E":
			break;
		case "S":
			directions[0] = "W";
			directions[1] = "S";
			directions[2] = "E";
			directions[3] = "N";
			break;
		case "W":
			directions[0] = "N";
			directions[1] = "W";
			directions[2] = "S";
			directions[3] = "E";
			break;
		case "N":
			directions[0] = "E";
			directions[1] = "N";
			directions[2] = "W";
			directions[3] = "S";
			break;
		default:
			break;
		}

		boolean hasMoved = false;
		for (int i = 0; i < directions.length; i++) {
			if (!isTowardsThisDirectionIsObsatacle(scanMapTiles, centerIndex,
					directions[i])) {
				System.out.println("(wall follower) move " + directions[i]);
				hasMoved = move(directions[i]);
				System.out.println("(WF) has moved? " + hasMoved);
				if (hasMoved) {

					return;
				} else {
					System.out.println("chose another direction.");
				}
			}
		}
	}

	void headEast(MapTile[][] scanMapTiles, int centerIndex) throws IOException {

		String[] directions = { "E", "S", "N" };

		boolean hasMoved = false;
		for (int i = 0; i < directions.length; i++) {
			if (!isTowardsThisDirectionIsObsatacle(scanMapTiles, centerIndex,
					directions[i])) {
				System.out.println("(wall follower) move " + directions[i]);
				hasMoved = move(directions[i]);
				System.out.println("(WF) has moved? " + hasMoved);
				if (hasMoved) {

					return;
				} else {
					System.out.println("chose another direction.");
				}
			}
		}
	}

	void headWest(MapTile[][] scanMapTiles, int centerIndex) throws IOException {

		String[] directions = { "W", "S", "N" };

		boolean hasMoved = false;
		for (int i = 0; i < directions.length; i++) {
			if (!isTowardsThisDirectionIsObsatacle(scanMapTiles, centerIndex,
					directions[i])) {
				System.out.println("(wall follower) move " + directions[i]);
				hasMoved = move(directions[i]);
				System.out.println("(WF) has moved? " + hasMoved);
				if (hasMoved) {

					return;
				} else {
					System.out.println("chose another direction.");
				}
			}
		}
	}

	void headSouth(MapTile[][] scanMapTiles, int centerIndex)
			throws IOException {

		String[] directions = { "S", "W", "E" };

		boolean hasMoved = false;
		for (int i = 0; i < directions.length; i++) {
			if (!isTowardsThisDirectionIsObsatacle(scanMapTiles, centerIndex,
					directions[i])) {
				System.out.println("(wall follower) move " + directions[i]);
				hasMoved = move(directions[i]);
				System.out.println("(WF) has moved? " + hasMoved);
				if (hasMoved) {

					return;
				} else {
					System.out.println("chose another direction.");
				}
			}
		}
	}

	void headNorth(MapTile[][] scanMapTiles, int centerIndex)
			throws IOException {

		String[] directions = { "N", "E", "W" };

		boolean hasMoved = false;
		for (int i = 0; i < directions.length; i++) {
			if (!isTowardsThisDirectionIsObsatacle(scanMapTiles, centerIndex,
					directions[i])) {
				System.out.println("(wall follower) move " + directions[i]);
				hasMoved = move(directions[i]);
				System.out.println("(WF) has moved? " + hasMoved);
				if (hasMoved) {

					return;
				} else {
					System.out.println("chose another direction.");
				}
			}
		}
	}

	void followLhsWall(MapTile[][] scanMapTiles, int centerIndex)
			throws IOException {

		String[] directions = { "N", "E", "S", "W" };
		switch (getFacingDirection()) {
		case "E":
			break;
		case "S":
			directions[0] = "E";
			directions[1] = "S";
			directions[2] = "W";
			directions[3] = "N";
			break;
		case "W":
			directions[0] = "S";
			directions[1] = "W";
			directions[2] = "N";
			directions[3] = "E";
			break;
		case "N":
			directions[0] = "W";
			directions[1] = "N";
			directions[2] = "E";
			directions[3] = "S";
			break;
		default:
			break;
		}

		boolean hasMoved = false;
		for (int i = 0; i < directions.length; i++) {
			if (!isTowardsThisDirectionIsObsatacle(scanMapTiles, centerIndex,
					directions[i])) {
				System.out.println("(wall follower) move " + directions[i]);
				hasMoved = move(directions[i]);
				System.out.println("(WF) has moved? " + hasMoved);
				if (hasMoved) {
					return;
				} else {
					System.out.println("chose another direction.");
				}
			}
		}
	}

	boolean isAllDirOpen(MapTile[][] scanMapTiles, int centerIndex) {
		System.out.println("is east blocked? "
				+ isTowardsThisDirectionIsObsatacle(scanMapTiles, centerIndex,
						"E"));
		System.out.println("is south blocked? "
				+ isTowardsThisDirectionIsObsatacle(scanMapTiles, centerIndex,
						"S"));
		System.out.println("is west blocked? "
				+ isTowardsThisDirectionIsObsatacle(scanMapTiles, centerIndex,
						"W"));
		System.out.println("is north blocked? "
				+ isTowardsThisDirectionIsObsatacle(scanMapTiles, centerIndex,
						"N"));
		// debu
		// try {
		// Thread.sleep(4000);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		return isTowardsThisDirectionIsObsatacle(scanMapTiles, centerIndex, "E")
				&& isTowardsThisDirectionIsObsatacle(scanMapTiles, centerIndex,
						"S")
				&& isTowardsThisDirectionIsObsatacle(scanMapTiles, centerIndex,
						"W")
				&& isTowardsThisDirectionIsObsatacle(scanMapTiles, centerIndex,
						"N");

	}

	void run() throws IOException, InterruptedException {
		ArrayList<String> equipment = new ArrayList<String>();
		boolean beenToTargetLoc = false;
		Socket socket = null;

		try {

			// ***** connect to server ******
			socket = connectToSwarmServer();

			// ***** get equipments ******
			equipment = getEquipment();

			// ***** initialize critical locations ******
			rovergroupStartPosition = requestStartLoc(socket);
			targetLocation = requestTargetLoc(socket);
			nextTarget = targetLocation.clone();

			/**
			 * #### Rover controller process loop ####
			 */
			boolean firstItr = true;
			Coord prevLoc = currentLoc.clone();
			cardinals[1] = true;
			int roverLogicSwitch = 0;
			int numLogic = 3;

			while (true) {

				setCurrentLoc(); // BEFORE the move() in this iteration
				pathMap.add(new Coord(currentLoc.xpos, currentLoc.ypos));
				System.out.println("BEFORE: " + currentLoc + " | facing "
						+ getFacingDirection());

				// ***** do a SCAN ******
				loadScanMapFromSwarmServer();

				MapTile[][] scanMapTiles = scanMap.getScanMap();
				int centerIndex = (scanMap.getEdgeSize() - 1) / 2;

				previousLoc = currentLoc;
				roverMotionLogic(cardinals, scanMapTiles, centerIndex,
						currentLoc.xpos, currentLoc.ypos);
				// followLhsWall(scanMapTiles, centerIndex);
				// headEast(scanMapTiles, centerIndex);
				// headWest(scanMapTiles, centerIndex);

				setCurrentLoc(); // AFTER this iteration
				System.out.println("AFTER: " + currentLoc);

				System.out
						.println("ROVER_12 ------------ bottom process control --------------");
				Thread.sleep(sleepTime);

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

	void test() throws IOException, InterruptedException {
		ArrayList<String> equipment = new ArrayList<String>();
		boolean beenToTargetLoc = false;
		Socket socket = null;
		InABeeLine cpu = new InABeeLine();

		try {

			// ***** connect to server ******
			socket = connectToSwarmServer();

			// ***** get equipments ******
			equipment = getEquipment();

			// ***** initialize critical locations ******
			rovergroupStartPosition = requestStartLoc(socket);
			targetLocation = requestTargetLoc(socket);
			nextTarget = targetLocation.clone();

			boolean firstItr = true;
			Coord prevLoc = currentLoc.clone();
			cardinals[1] = true;
			int roverLogicSwitch = 0;
			int numLogic = 3;

			/**
			 * #### Rover controller process loop ####
			 */

			loadScanMapFromSwarmServer();

			debugPrintMapTileArray(mapTileLog);
			cpu.getShortestPath(currentLoc, new Coord(7, 2),mapTileLog);
			System.out
					.println("ROVER_12 ------------ bottom process control --------------");
			Thread.sleep(10000);

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
	}// END of test()

	void test2() throws IOException, InterruptedException {
		ArrayList<String> equipment = new ArrayList<String>();
		boolean beenToTargetLoc = false;
		Socket socket = null;
		InABeeLine cpu = new InABeeLine();

		try {

			// ***** connect to server ******
			socket = connectToSwarmServer();

			// ***** get equipments ******
			equipment = getEquipment();

			// ***** initialize critical locations ******
			rovergroupStartPosition = requestStartLoc(socket);
			targetLocation = requestTargetLoc(socket);
			nextTarget = targetLocation.clone();

			loadScanMapFromSwarmServer();
			System.out.println("nearest obstacle: "
					+ outwardSpiralSearch(currentLoc));
			System.out.println(outwardSpiralSearch(currentLoc));
			Thread.sleep(10000);
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
	}// END of test2()

	void debugSandAvoidanceMotion(MapTile[][] scanMapTiles, int centerIndex)
			throws IOException, InterruptedException {

		if (currentLoc.xpos == 10 && currentLoc.ypos < 15) {
			if (!isTowardsThisDirectionIsObsatacle(scanMapTiles, centerIndex,
					"S")) {
				System.out.println("move south");
				move("S");
			}
		} else if (currentLoc.xpos >= 13 && currentLoc.xpos <= 15
				&& currentLoc.ypos >= 14 && currentLoc.ypos < 17
				|| currentLoc.xpos >= 13 && currentLoc.xpos <= 17
				|| currentLoc.xpos >= 14 && currentLoc.xpos <= 15) {

			System.out.println("move random");
			randomStep(scanMapTiles, centerIndex);

		} else if (!isTowardsThisDirectionIsObsatacle(scanMapTiles,
				centerIndex, "E")) {
			System.out.println("move east");
			move("E");
		} else if (!isTowardsThisDirectionIsObsatacle(scanMapTiles,
				centerIndex, "S")) {
			System.out.println("move south");
			move("S");
		} else if (!isTowardsThisDirectionIsObsatacle(scanMapTiles,
				centerIndex, "W")) {
			System.out.println("move south");
			move("W");
		} else {
			System.out.println("move south");
			move("N");
		}
	}

	Socket connectToSwarmServer() throws UnknownHostException, IOException {
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

	boolean isTowardsWestIsObsatacle(MapTile[][] scanMapTiles, int centerIndex) {
		if (scanMapTiles[centerIndex - 1][centerIndex].getHasRover()
				|| scanMapTiles[centerIndex - 1][centerIndex].getTerrain() == Terrain.ROCK
				|| scanMapTiles[centerIndex - 1][centerIndex].getTerrain() == Terrain.NONE
				|| scanMapTiles[centerIndex - 1][centerIndex].getTerrain() == Terrain.FLUID
				|| scanMapTiles[centerIndex - 1][centerIndex].getTerrain() == Terrain.SAND
				|| scanMapTiles[centerIndex + 1][centerIndex].getHasRover()) {
			return true;
		}
		return false;
	}

	public boolean isObsatacle(Coord focus) {
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

	boolean isTowardsEastIsObsatacle(MapTile[][] scanMapTiles, int centerIndex) {
		if (scanMapTiles[centerIndex + 1][centerIndex].getHasRover()
				|| scanMapTiles[centerIndex + 1][centerIndex].getTerrain() == Terrain.ROCK
				|| scanMapTiles[centerIndex + 1][centerIndex].getTerrain() == Terrain.NONE
				|| scanMapTiles[centerIndex + 1][centerIndex].getTerrain() == Terrain.FLUID
				|| scanMapTiles[centerIndex + 1][centerIndex].getTerrain() == Terrain.SAND) {
			return true;
		} else {
			return false;
		}
	}

	boolean isTowardsNorthIsObsatacle(MapTile[][] scanMapTiles, int centerIndex) {
		if (scanMapTiles[centerIndex][centerIndex - 1].getHasRover()
				|| scanMapTiles[centerIndex][centerIndex - 1].getTerrain() == Terrain.ROCK
				|| scanMapTiles[centerIndex][centerIndex - 1].getTerrain() == Terrain.NONE
				|| scanMapTiles[centerIndex][centerIndex - 1].getTerrain() == Terrain.FLUID
				|| scanMapTiles[centerIndex][centerIndex - 1].getTerrain() == Terrain.SAND) {
			return true;
		}
		return false;
	}

	boolean isTowardsSouthIsObsatacle(MapTile[][] scanMapTiles, int centerIndex) {
		if (scanMapTiles[centerIndex][centerIndex + 1].getHasRover()
				|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.ROCK
				|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.NONE
				|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.FLUID
				|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.SAND) {
			return true;
		}
		return false;
	}

	boolean isTowardsThisDirectionIsObsatacle(MapTile[][] scanMapTiles,
			int centerIndex, String dir) {
		switch (dir) {
		case "E":
			return isTowardsEastIsObsatacle(scanMapTiles, centerIndex);

		case "S":
			return isTowardsSouthIsObsatacle(scanMapTiles, centerIndex);

		case "W":
			return isTowardsWestIsObsatacle(scanMapTiles, centerIndex);

		case "N":
			return isTowardsNorthIsObsatacle(scanMapTiles, centerIndex);
		default:
			return true;
		}
	}

	void setCurrentLoc() throws IOException {

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

	Coord requestCurrentLoc() throws IOException {

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

	void clearReadLineBuffer() throws IOException {
		while (in.ready()) {
			// System.out.println("ROVER_12 clearing readLine()");
			in.readLine();
		}
	}

	// method to retrieve a list of the rover's EQUIPMENT from the server
	ArrayList<String> getEquipment() throws IOException {
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
	// sets current location each time this function is called
	public void loadScanMapFromSwarmServer() throws IOException {

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

		scanMap = gson.fromJson(jsonScanMapString, ScanMap.class);

		// myJSONStringBackupofMap = jsonScanMapString;
		loadMapTilesOntoGlobalMapLog(scanMap.getScanMap(), scanLoc);
	}

	Coord requestStartLoc(Socket soc) throws IOException {

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

	Coord requestTargetLoc(Socket soc) throws IOException {

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

	int requestTimeRemaining(Socket soc) throws IOException {

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

	static Coord extractCurrLOC(String sStr) {
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
	void updateFromGreenCorpGlobalMap(JSONArray data) {

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

	static Coord extractStartLOC(String sStr) {

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

	static Coord extractTargetLOC(String sStr) {
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

	static int extractTimeRemaining(String sStr) {
		sStr = sStr.substring(6);
		if (sStr.lastIndexOf(" ") != -1) {
			String timeStr = sStr.substring(0, sStr.lastIndexOf(" "));
			return Integer.parseInt(timeStr);
		}
		return -1;
	}

	// this takes the server response string, parses out the x and x values and
	// returns a Coord object
	static Coord extractLocationFromString(String sStr) {
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
		int edgeSize = 20;
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

	void recordPath() {

	}

	void loadMapTilesOntoGlobalMapLog(MapTile[][] ptrScanMap, Coord scanLoc) {

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

	boolean move(String dir) throws IOException {

		MapTile[][] scanMapTiles = scanMap.getScanMap();
		int centerIndex = (scanMap.getEdgeSize() - 1) / 2;

		switch (dir) {
		case "E":
			if (!isTowardsThisDirectionIsObsatacle(scanMapTiles, centerIndex,
					"E")) {
				moveEast();
				System.out.println("moved east, return true");
				return true;
			}
		case "W":
			if (!isTowardsThisDirectionIsObsatacle(scanMapTiles, centerIndex,
					"W")) {
				moveWest();
				System.out.println("moved west, return true");
				return true;
			}
			break;
		case "N":
			if (!isTowardsThisDirectionIsObsatacle(scanMapTiles, centerIndex,
					"N")) {
				moveNorth();
				System.out.println("moved north, return true");
				return true;
			}
			break;
		case "S":
			if (!isTowardsThisDirectionIsObsatacle(scanMapTiles, centerIndex,
					"S")) {
				moveSouth();
				System.out.println("moved south, return true");
				return true;
			}
			break;
		default:
			return false;
		}
		return false;
	}

	void moveWest() {
		out.println("MOVE W");
		System.out.println("ROVER_12 request move W");
		cardinals[0] = false; // S
		cardinals[1] = false; // E
		cardinals[2] = false; // N
		cardinals[3] = true; // W
	}

	void moveNorth() {
		out.println("MOVE N");
		System.out.println("ROVER_12 request move N");
		cardinals[0] = false; // S
		cardinals[1] = false; // E
		cardinals[2] = true; // N
		cardinals[3] = false; // W
	}

	void moveSouth() {
		out.println("MOVE S");
		System.out.println("ROVER_12 request move S");
		cardinals[0] = true; // S
		cardinals[1] = false; // E
		cardinals[2] = false; // N
		cardinals[3] = false; // W
	}

	void moveEast() {
		out.println("MOVE E");
		System.out.println("ROVER_12 request move E");
		cardinals[0] = false; // S
		cardinals[1] = true; // E
		cardinals[2] = false; // N
		cardinals[3] = false; // W
	}

	// a check function to prevent IndexOutOfBounds exception
	public boolean isWithinTheGrid(int i, int j, int arrayLength) {
		return i >= 0 && j >= 0 && i < arrayLength && j < arrayLength;
	}

	void sendJSONToServer(JSONObject obj, String URL) {
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

	public String getFacingDirection() {
		if (cardinals[0] == true) {
			return "S";
		}
		if (cardinals[1] == true) {
			return "E";
		}
		if (cardinals[2] == true) {
			return "N";
		}
		return "W";

	}

	void shuffuleArray(String[] directions) {
		// Thanks, Fisher-Yates shuffle
		int idx;
		String temp;

		for (int i = directions.length - 1; i > -1; i--) {
			idx = randomNum(0, i);
			temp = directions[idx];
			directions[idx] = directions[i];
			directions[i] = temp;
		}
	}

	// take a random step (just one step) to break the pattern
	void randomStep(MapTile[][] scanMapTiles, int centerIndex)
			throws InterruptedException, IOException {

		String currDir = getFacingDirection();
		String[] directions = { "N", "E", "S", "W" };
		shuffuleArray(directions);

		for (String thisDir : directions) {
			if (!currDir.equals(thisDir)) {
				System.out.println("move " + thisDir);
				move(thisDir);
				return;
			}

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

	// ****** under construction
	// public Coord getNextTargetCoord() {
	//
	// boolean isTargetLocReached = !mapTileLog.containsKey(targetLocation);
	// int searchSize = 30, nullCounter = 0;
	// // Coord nextTarget= new Coord(randomNum(min, max));
	//
	// if (!visited(targetLocation)) {
	// return targetLocation;
	// }
	//
	// // while()
	// if (visitCounts.size() < 1) {
	//
	// }
	// return null;
	// }

	boolean[] randomPickMotion(boolean[] cardinals, int centerIndex,
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

	boolean isAlreadyTraveledPathTowardsWest(int currentXPos, int currentYPos) {
		int nextXPosition = currentXPos - 1;
		int nextYPosition = currentYPos;
		for (Coord coord : pathMap) {
			if ((coord.xpos == nextXPosition) && (coord.ypos == nextYPosition)) {
				return true;
			}
		}
		return false;
	}

	boolean isAlreadyTraveledPathTowardsNorth(int currentXPos, int currentYPos) {
		int nextXPosition = currentXPos;
		int nextYPosition = currentYPos - 1;
		for (Coord coord : pathMap) {
			if ((coord.xpos == nextXPosition) && (coord.ypos == nextYPosition)) {
				return true;
			}
		}
		return false;
	}

	boolean isAlreadyTraveledPathTowardsSouth(int currentXPos, int currentYPos) {
		int nextXPosition = currentXPos;
		int nextYPosition = currentYPos + 1;
		for (Coord coord : pathMap) {
			if ((coord.xpos == nextXPosition) && (coord.ypos == nextYPosition)) {
				return true;
			}
		}
		return false;
	}

	boolean isAlreadyTraveledPathTowardsEast(int currentXPos, int currentYPos) {
		int nextXPosition = currentXPos + 1;
		int nextYPosition = currentYPos;
		for (Coord coord : pathMap) {
			if ((coord.xpos == nextXPosition) && (coord.ypos == nextYPosition)) {
				return true;
			}
		}
		return false;
	}

	// return the nearest wall coord
	public Coord outwardSpiralSearch(Coord curr) throws Exception {

		int searchSize = 10;
		String[] directions = { "E", "S", "W", "N" };
		Coord topL, bottomR, temp;
		int x, y, xx, yy;

		for (int i = 1; i <= searchSize; i++) {
			topL = new Coord(curr.xpos - i, curr.ypos - i);
			bottomR = new Coord(curr.xpos + i, curr.ypos + i);

			// north edge
			x = topL.xpos;
			y = topL.ypos;
			for (xx = x; xx <= bottomR.xpos; xx++) {

				temp = new Coord(xx, y);
				if (isWithinTheGrid(xx, y, 50) && isObsatacle(temp)) {
					return temp;
				}
			}
			// east edge
			x = bottomR.xpos;
			y = topL.ypos + 1;
			for (yy = y; yy <= bottomR.ypos; yy++) {

				temp = new Coord(x, yy);
				if (isWithinTheGrid(x, yy, 50) && isObsatacle(temp)) {
					return temp;
				}
			}
			// south edge
			x = bottomR.xpos - 1;
			y = bottomR.ypos;
			for (xx = x; xx >= topL.xpos; xx--) {

				temp = new Coord(xx, y);
				if (isWithinTheGrid(xx, y, 50) && isObsatacle(temp)) {
					return temp;
				}
			}
			// west edge
			x = topL.xpos;
			y = bottomR.ypos - 1;
			for (yy = y; yy > topL.ypos; yy--) {

				temp = new Coord(x, yy);
				if (isWithinTheGrid(x, yy, 50) && isObsatacle(temp)) {
					return temp;
				}
			}
		}

		return null;
	}

	// given two coordinates, pick a next move-direction 
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

	
	/**
	 * Runs the client
	 */
	public static void main(String[] args) throws Exception {
		Rv_curr client = new Rv_curr();
		// client.test2(); // outward search test
		 client.test();// astar test
	}
}