package swarmBots;

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
import java.util.Collections;
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
import rover_kae.InABeeLine8Dir;
import rover_kae.Node;
import supportTools.CommunicationUtil;
import supportTools.Path;
import supportTools.RoverMotionUtil;

/**
 * The seed that this program is built on is a chat program example found here:
 * http://cs.lmu.edu/~ray/notes/javanetexamples/ Many thanks to the authors for
 * publishing their code examples
 */

public class ROVER_12_wk9 {

	private BufferedReader in;
	private PrintWriter out;
	private String rovername;
	private ScanMap scanMap;
	private int sleepTime;
	// private String SERVER_ADDRESS = "localhost", line;
	private String SERVER_ADDRESS = "192.168.1.106", line;
	static final int PORT_ADDRESS = 9537;

	// Group 12 variables
	int numLogics = 3;
	static String myJSONStringBackupofMap;
	private Coord currentLoc, previousLoc, rovergroupStartPosition = null,
			targetLocation = null;

	private Map<Coord, MapTile> mapTileLog = new HashMap<Coord, MapTile>();
	public ArrayList<Coord> pathMap = new ArrayList<Coord>();

	private Random rd = new Random();
	private boolean[] cardinals = new boolean[4];
	private boolean isTargetLocReached = false;
	private Coord nextTarget;

	public ROVER_12_wk9() {
		// constructor
		System.out.println("ROVER_12 rover object constructed");
		rovername = "ROVER_12";
		// SERVER_ADDRESS = "192.168.1.106";
		SERVER_ADDRESS = "localhost";
		// this should be a safe but slow timer value
		sleepTime = 500; // in milliseconds - smaller is faster, but the server
							// will cut connection if it is too small
	}

	public ROVER_12_wk9(String serverAddress) {
		// constructor
		System.out.println("ROVER_12 rover object constructed");
		rovername = "ROVER_12";
		SERVER_ADDRESS = serverAddress;
		sleepTime = 500; // in milliseconds - smaller is faster, but the server
							// will cut connection if it is too small
	}

	// ******* Communications *********************
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

	// Thanks for Group 11 and their Communication class which made this more
	// neat
	private JSONArray convertScanMapTiles(Coord currentLoc,
			MapTile[][] scanMapTiles) {
		int edgeSize = scanMapTiles.length;
		int centerIndex = (edgeSize - 1) / 2;

		JSONArray tiles = new JSONArray();
		for (int row = 0; row < scanMapTiles.length; row++) {
			for (int col = 0; col < scanMapTiles[row].length; col++) {

				MapTile mapTile = scanMapTiles[col][row];

				int xp = currentLoc.xpos - centerIndex + col;
				int yp = currentLoc.ypos - centerIndex + row;
				Coord coord = new Coord(xp, yp);
				JSONObject tile = new JSONObject();
				tile.put("x", xp);
				tile.put("y", yp);
				tile.put("terrain", mapTile.getTerrain().toString());
				tile.put("science", mapTile.getScience().toString());
				tiles.add(tile);
			}
		}
		return tiles;
	}

	// HTTP POST USING Apache HTTP client library
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
	public void sendPost(JSONObject jsonObj) throws Exception {

		String url = "http://localhost:3000/api";
		String corp_secret = "XXXXX";

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		String USER_AGENT = "ROVER 12";
		// add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		con.setDoOutput(true);

		// Solution from Group 11
		// byte[] jsonBytes = data.toString().getBytes("UTF-8");

		// Send post request
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());

		// Solution by Group 11
		// wr.write(jsonBytes);
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

	// ******* Rover Motion 0 *********************
	private void sinusoidal_RtoL(String[] cardinals, int waveLength,
			int waveHeight) throws InterruptedException, IOException {
		int steps, trackNoMove;

		steps = waveLength;
		String currentDir;
		cardinals[0] = "E";
		cardinals[1] = "S";
		cardinals[2] = "E";
		cardinals[3] = "N";

		if (previousLoc != null && isStuck(currentLoc, previousLoc)) {
			sinusoidal_RtoL(cardinals, waveLength, waveHeight);
		}

		previousLoc = currentLoc;

		for (int i = 0; i < cardinals.length; i++) {

			currentDir = cardinals[i];
			if (currentDir.equals("E") || currentDir.equals("E")) {
				steps = waveLength;
			} else {
				steps = waveHeight;
			}

			for (int j = 0; j < steps; j++) {

				setCurrentLoc();
				hasMoved = move(currentDir);
				if (!hasMoved) {
					trackNoMove++;
				}
				Thread.sleep(sleeptime);
			}
		}
	}

	private void sinusoidal_LtoR(String[] cardinals, int waveLength,
			int waveHeight) throws InterruptedException, IOException {
		int steps;

		steps = waveLength;
		String currentDir;
		cardinals[0] = "E";
		cardinals[1] = "S";
		cardinals[2] = "E";
		cardinals[3] = "N";

		if (previousLoc != null && isStuck(currentLoc, previousLoc)) {
			sinusoidal_RtoL(cardinals, waveLength, waveHeight);
		}

		previousLoc = currentLoc;

		for (int i = 0; i < cardinals.length; i++) {

			currentDir = cardinals[i];
			if (currentDir.equals("E") || currentDir.equals("E")) {
				steps = waveLength;
			} else {
				steps = waveHeight;
			}

			for (int j = 0; j < steps; j++) {

				setCurrentLoc();
				hasMoved = move(currentDir);
				if (!hasMoved) {
					trackNoMove++;
				}
				Thread.sleep(sleeptime);
			}
		}
	}

	// ******* Rover Motion 1 *********************
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
				setCurrentLoc();

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

	// return the nearest obstacle coord
	public Coord outwardSpiralSearch(Coord curr) throws Exception {

		int searchSize = 10;
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

	// ******* Rover Motion 2 *********************
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

	private void followRhsWall(MapTile[][] scanMapTiles, int centerIndex)
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

	private void headEast(MapTile[][] scanMapTiles, int centerIndex)
			throws IOException {

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

	private void headWest(MapTile[][] scanMapTiles, int centerIndex)
			throws IOException {

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

	private void headSouth(MapTile[][] scanMapTiles, int centerIndex)
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

	private void headNorth(MapTile[][] scanMapTiles, int centerIndex)
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

	private void followLhsWall(MapTile[][] scanMapTiles, int centerIndex)
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

	private boolean isAllDirOpen(MapTile[][] scanMapTiles, int centerIndex) {
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
		String url = "http://23.251.155.186:3000/api";
		String corp_secret = "0FSj7Pn23t";
		Communication com = new Communication(url, rovername, corp_secret);

		new ArrayList<String>();
		Socket socket = null;
		boolean astarGo = false;
		int pedometer = 0;

		try {

			// ***** connect to server ******
			socket = connectToSwarmServer();

			getEquipment();

			// ***** initialize critical locations ******
			rovergroupStartPosition = requestStartLoc(socket);
			targetLocation = requestTargetLoc(socket);
			nextTarget = targetLocation.clone();

			currentLoc.clone();
			cardinals[1] = true;
			while (true) {

				setCurrentLoc(); // BEFORE the move() in this iteration
				pathMap.add(new Coord(currentLoc.xpos, currentLoc.ypos));
				System.out.println("BEFORE: " + currentLoc + " | facing "
						+ getFacingDirection());

				// ***** do a SCAN ******
				loadScanMapFromSwarmServer();

				MapTile[][] scanMapTiles = scanMap.getScanMap();
				int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
				com.postScanMapTiles(currentLoc, scanMapTiles);
				
				astarGo = (pedometer > 95 && pedometer % 35 == 1) ? true
						: false;

				
				if (pedometer > 95 && pedometer % 35 == 1) {
					if (mapTileLog.get(targetLocation) == null) {
						System.out.println("go astar!");
						astarGo = true;
						astar();
					}
				}

					roverMotionLogic(cardinals, scanMapTiles, centerIndex,
							currentLoc.xpos, currentLoc.ypos);
				

				setCurrentLoc();
				pedometer++;
			
				System.out
						.println("ROVER_12 ------------ bottom process control --------------");
				Thread.sleep(sleepTime);

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

	private void debugSandAvoidanceMotion(MapTile[][] scanMapTiles,
			int centerIndex) throws IOException, InterruptedException {

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

	private boolean isTowardsWestIsObsatacle(MapTile[][] scanMapTiles,
			int centerIndex) {
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

	private boolean isTowardsEastIsObsatacle(MapTile[][] scanMapTiles,
			int centerIndex) {
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

	private boolean isTowardsThisDirectionIsObsatacle(MapTile[][] scanMapTiles,
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

	public String[] getShortestPath(Coord start, Coord goal,
			Map<Coord, MapTile> mapTileLog) throws Exception {
		shortestPath.clear();
		StringBuffer sb = new StringBuffer();
		Map<Coord, Node> open = new HashMap<Coord, Node>();
		Deque<Node> closed = new ArrayDeque<>();
		Map<Coord, Node> nodeComputed = new HashMap<Coord, Node>();
		Node cheapest;
		Node s = new Node(start, null);
		Node g = new Node(goal, null);
		Node center;
		s.setF(computeF(s, s, g));

		// add start tile to closed
		center = s.clone();
		if (!open.containsKey(start)) {
			open.put(start, center);
		}
		closed.push(s);

		int itrTracker = 0;

		if (center == null || open == null) {
			System.out.println("NULLNULLNULL");
			String[] aPath = { "no solution" };
			return aPath;
		}

		while (!center.coord.equals(goal) && !open.isEmpty()) {
			itrTracker++;
			cheapest = computeAdjacents(center, s, g, nodeComputed, open,
					closed, mapTileLog);
			center = cheapest;

			// debug p out
			System.out.println("this itr[" + itrTracker + "]:\ncoord "
					+ center.coord + "\ncheapest of open " + cheapest
					+ "\nsize of open " + open.size() + "\ncurr center "
					+ center.str());

			closed.push(cheapest);
			open.remove(center);
			if (cheapest == null || center == null || open == null) {
				System.out.println("NULLNULLNULL");
				String[] aPath = { "no solution" };
				return aPath;
			}
		}

		for (Node node : closed) {
			System.out.println("(close)" + node);
			if (node.parentNode != null
					&& !shortestPath.contains(node.parentNode)) {
				shortestPath.add(node.parentNode);

			}
		}

		// THIS NEEDS TO BE KEPT!
		Collections.reverse(shortestPath);

		System.out.println("\nreversed ");
		for (Node node : shortestPath) {
			System.out.println(node + " ");
		}
		for (int i = 0; i < shortestPath.size(); i++) {
			System.out.println("" + i + ": " + shortestPath.get(i).coord);
		}

		// -----------------------
		String thisStr = "";
		Coord prev = start.clone();
		for (Node node : shortestPath) {

			if (node.parentNode != null) {
				System.out.println("(sp str build) FROM "
						+ node.parentNode.coord + " TO " + node.coord);

				// get the direction from the parent node to this node
				thisStr = coordToDir(node.parentNode.coord, node.coord,
						mapTileLog);

				sb.append(thisStr);
			}
			prev = node.coord.clone();
		}

		if (shortestPath != null
				&& shortestPath.get(shortestPath.size() - 1).parentNode != null) {
			// sb.append(coordToDir(
			// shortestPath.get(shortestPath.size() - 1).parentNode.coord,
			// shortestPath.get(shortestPath.size() - 1).coord, mapTileLog));
			sb.append(coordToDir(
					shortestPath.get(shortestPath.size() - 1).coord, goal,
					mapTileLog));
			System.out.println("direction string:" + sb.toString());
		}
		String[] aPath = new String[(sb.length()) + 1];
		for (int i = 0; i < sb.length(); i++) {
			thisStr = sb.substring(i, i + 1);
			if (thisStr.equals("stop")) {
				break;
			}
			aPath[i] = thisStr;
			System.out.print("(" + i + ")" + aPath[i] + " ");
		}
		aPath[aPath.length - 1] = "end";
		System.out.println("(" + (aPath.length - 1) + ")"
				+ aPath[aPath.length - 1]);

		return aPath;
	}

	public String coordToDir(Coord from, Coord to,
			Map<Coord, MapTile> mapTileLog) {
		StringBuffer sb = new StringBuffer();
		System.out.println("(coordToDir())get the dir from " + from + " to "
				+ to);
		int dx = to.xpos - from.xpos;
		int dy = to.ypos - from.ypos;
		int xCount = Math.abs(dx);
		int yCount = Math.abs(dy);

		// horizontal motion
		if (dy == 0) {
			if (dx > 0) {
				System.out.println("e");
				sb.append("E");
			} else {
				System.out.println("w");
				sb.append("W");
			}
		}

		// vertical motion
		else if (dx == 0) {
			if (dy > 0) {
				System.out.println("s");
				sb.append("S");
			} else {
				System.out.println("e");
				sb.append("E");
			}
		}

		// diagonal motion
		else {
			// ne
			if (dx > 0 && dy < 0) {
				if (!isObsatacle(new Coord(from.xpos + 1, from.ypos),
						mapTileLog)) {
					System.out.println("en");
					sb.append("E");
					sb.append("N");
				} else {
					System.out.println("ne");
					sb.append("N");
					sb.append("E");
				}
			}
			// se
			else if (dx > 0 && dy > 0) {
				if (!isObsatacle(new Coord(from.xpos + 1, from.ypos),
						mapTileLog)) {
					System.out.println("es");
					sb.append("E");
					sb.append("S");
				} else {
					System.out.println("se");
					sb.append("S");
					sb.append("E");
				}
			}
			// nw
			else if (dx < 0 && dy < 0) {
				if (!isObsatacle(new Coord(from.xpos - 1, from.ypos),
						mapTileLog)) {
					System.out.println("wn");
					sb.append("W");
					sb.append("N");
				} else {
					System.out.println("nw");
					sb.append("N");
					sb.append("W");
				}
			}
			// sw
			else if (dx < 0 && dy > 0) {
				if (!isObsatacle(new Coord(from.xpos - 1, from.ypos),
						mapTileLog)) {
					System.out.println("ws");
					sb.append("W");
					sb.append("S");
				} else {
					System.out.println("sw");
					sb.append("S");
					sb.append("W");
				}
			}
		}

		return sb.toString();
	}

	private boolean hasAllTileInfo(int tlX, int tlY, int brX, int brY,
			Map<Coord, MapTile> mapTileLog) {

		for (int j = tlY; j < brY; j++) {
			for (int i = tlX; i < brX; i++) {
				if (!mapTileLog.containsKey(new Coord(i, j))) {
					return false;
				}
			}
		}
		return true;
	}

	// get the least expensive adjacent
	public Node computeAdjacents(Node center, Node start, Node goal,
			Map<Coord, Node> nodesComputed, Map<Coord, Node> open,
			Deque<Node> closed, Map<Coord, MapTile> mapTileLog) {

		open.remove(center.coord);
		System.out.println("inside computeAdjacents()\tcenter:" + center.str());

		List<Node> adjacents = new ArrayList<Node>();
		int x = center.coord.xpos;
		int y = center.coord.ypos;

		Coord n = new Coord(x, y - 1);
		examineThisAdjacent(center, goal, nodesComputed, open, closed,
				mapTileLog, adjacents, n);

		Coord ne = new Coord(x + 1, y - 1);
		examineThisAdjacent(center, goal, nodesComputed, open, closed,
				mapTileLog, adjacents, ne);

		Coord e = new Coord(x + 1, y);
		examineThisAdjacent(center, goal, nodesComputed, open, closed,
				mapTileLog, adjacents, e);

		Coord se = new Coord(x + 1, y + 1);
		examineThisAdjacent(center, goal, nodesComputed, open, closed,
				mapTileLog, adjacents, se);

		Coord s = new Coord(x, y + 1);
		examineThisAdjacent(center, goal, nodesComputed, open, closed,
				mapTileLog, adjacents, s);

		Coord sw = new Coord(x - 1, y + 1);
		examineThisAdjacent(center, goal, nodesComputed, open, closed,
				mapTileLog, adjacents, sw);

		Coord w = new Coord(x - 1, y);
		examineThisAdjacent(center, goal, nodesComputed, open, closed,
				mapTileLog, adjacents, w);

		Coord nw = new Coord(x - 1, y - 1);
		examineThisAdjacent(center, goal, nodesComputed, open, closed,
				mapTileLog, adjacents, nw);

		// debug print out
		for (Node node : adjacents) {
			System.out.println("adj " + node);
		}
		return min(open);
	}

	private void examineThisAdjacent(Node center, Node goal,
			Map<Coord, Node> nodesComputed, Map<Coord, Node> open,
			Deque<Node> closed, Map<Coord, MapTile> mapTileLog,
			List<Node> adjacents, Coord adjacent) {

		int thisG = -1;
		Node thisAdj;
		
		// if this node is not an obstacle or the one in the closed list
		if (!closed.contains(adjacent) && !isObsatacle(adjacent, mapTileLog)) {

			// if this node has been examined already w/ a different center
			if (nodesComputed.containsKey(adjacent)) {

				// is the stored g greater than newly computed g?
				thisAdj = nodesComputed.get(adjacent);
				thisG = computeG(center, thisAdj);
				if (thisG < thisAdj.g) {
					// if so,update the parent and g
					thisAdj.setParent(center);
					thisAdj.setG(thisG);
				}
			} else {
				Node node = new Node(adjacent, center, -1);
				node.setF(computeF(node, center, goal));
				System.out.println("this adj: " + node.str());

				adjacents.add(node);
				nodesComputed.put(node.coord, node);
				if (!open.containsKey(node.coord)) {
					open.put(node.coord, node);
				}
			}
		}
	}

	public void debugPrintAdjacents(Map<Coord, Node> adj) {
		for (Node node : adj.values()) {
			System.out.println(node);
		}
	}

	// start -> goal distance
	public void setH(Node focus, Node goal) {

		int dx = Math.abs(focus.coord.xpos - goal.coord.xpos);
		int dy = Math.abs(focus.coord.ypos - goal.coord.ypos);

		focus.h = (dx + dy) * 10;
	}

	// start -> goal distance
	public int computeH(Coord focus, Coord goal) {

		System.out.println("here: " + focus + "\t there: " + goal);
		int dx = Math.abs(focus.xpos - goal.xpos);
		int dy = Math.abs(focus.ypos - goal.ypos);

		return (dx + dy) * 10;
	}

	// start -> focus (movement cost)
	public void setG(Node center, Node focus) {

		if (center.equals(focus)) {
			focus.g = 0;
			return;
		}

		int cx = center.coord.xpos;
		int cy = center.coord.ypos;
		int fx = focus.coord.xpos;
		int fy = focus.coord.ypos;

		// diagonal cells: 14 because sqrt(10*10 + 10*10) = 1.414...
		int baseVal = 14;
		// verical or horizontalcell cells
		if (cx == fx || cy == fy) {
			baseVal = 10;
		}

		int g = center.g + baseVal;
		focus.g = g;
	}

	public int computeG(Node center, Node focus) {

		if (center.equals(focus)) {
			focus.g = 0;
			return 0;
		}

		int cx = center.coord.xpos;
		int cy = center.coord.ypos;
		int fx = focus.coord.xpos;
		int fy = focus.coord.ypos;

		// diagonal cells: 14 because sqrt(10*10 + 10*10) = 1.414...
		int baseVal = 14;
		// verical or horizontalcell cells
		if (cx == fx || cy == fy) {
			baseVal = 10;
		}

		int g = center.g + baseVal;
		return g;
	}

	public int computeF(Node focus, Node center, Node goal) {

		setH(focus, goal);
		setG(center, focus);

		return (focus.h + focus.g);
	}

	private Node min(Map<Coord, Node> open) {
		Node min = new Node();
		int minVal = Integer.MAX_VALUE, thisVal = 0;

		for (Node node : open.values()) {
			thisVal = node.f;
			if (minVal > thisVal) {
				min = node;
				minVal = thisVal;
			}
		}

		return min;
	}

	// expensive distance computation (Pythagorean)
	private double getDistanceBtw2Points(Coord p1, Coord p2) {

		int dx = p2.xpos - p1.xpos;
		int dy = p2.ypos - p1.ypos;

		return Math.sqrt((dx * dx) + (dy * dy));
	}

	// args must be adjacent two points
	private String getDir(Coord p1, Coord p2) {

		int dx = p2.xpos - p1.xpos;
		int dy = p2.ypos - p1.ypos;

		if (dx > 0) {
			return "E";
		}
		if (dx < 0) {
			return "W";
		}
		if (dy > 0) {
			return "S";
		} else {
			// dy < 0
			return "N";
		}
	}

	public void printCells(Map<Coord, Node> nodeComputed) {
		Node node;
		for (int j = 0; j < 2; j++) {
			for (int i = 0; i < 2; i++) {
				node = nodeComputed.get(new Coord(i, j));
				if (node != null) {
					System.out.print(node.str());
				}
			}
			System.out.println();
		}
	}

	public boolean isObsatacle(Coord focus, Map<Coord, MapTile> mapTileLog) {
		MapTile tile = mapTileLog.get(focus);

		// we don't have the log for this tile
		if (tile == null) {
			return true;
		}

		if (tile.getHasRover() || tile.getTerrain() == Terrain.ROCK
				|| tile.getTerrain() == Terrain.NONE
				|| tile.getTerrain() == Terrain.FLUID
				|| tile.getTerrain() == Terrain.SAND) {
			return true;
		} else {
			return false;
		}
	}

	public Coord[] getSearchArea(Coord p1, Coord p2, int k) {

		// given 2 points, find top-left-corner and bottom-right-corner
		int tlX = (p1.xpos < p2.xpos) ? p1.xpos : p2.xpos;
		int tlY = (p1.ypos < p2.ypos) ? p1.ypos : p2.ypos;
		int brX = (p1.xpos > p2.xpos) ? p1.xpos : p2.xpos;
		int brY = (p1.ypos > p2.ypos) ? p1.ypos : p2.ypos;

		// decrement top-left by k, increment bottom-right by k
		tlX -= k;
		tlY -= k;
		brX -= k;
		brY -= k;

		Coord[] corners = { new Coord(tlX, tlY), new Coord(brX, brY) };
		return corners;
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

	private boolean move(String dir) throws IOException {

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

	private void shuffuleArray(String[] directions) {
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
	private void randomStep(MapTile[][] scanMapTiles, int centerIndex)
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

		// take in first input argument as a SERVER_ADDRESS value
		String serverAddress = "";
		for (String s : args) {
			serverAddress = s;
		}

		ROVER_12_wk9 client = new ROVER_12_wk9(serverAddress);
		client.run();
	}

}
