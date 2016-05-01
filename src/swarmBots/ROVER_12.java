package swarmBots;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import swarmBots.NextMoveModel;

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

public class ROVER_12 {

	BufferedReader in;
	PrintWriter out;
	String rovername;
	ScanMap scanMap;
	int sleepTime;
	String SERVER_ADDRESS = "localhost", line;
	static final int PORT_ADDRESS = 9537;
	static String myJSONStringBackupofMap;
	Coord currentLoc, previousLoc, rovergroupStartPosition = null,
			targetLocation = null;
	Map<Coord, MapTile> mapTileLog = new HashMap<Coord, MapTile>();
	public ArrayList<Coord> pathMap = new ArrayList<Coord>();

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

			String line = "";

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
			// Thread.sleep(10000);

			boolean goingSouth = false;
			boolean goingEast = true;
			boolean goingNorth = false;
			boolean goingWest = true;
			boolean stuck = false; // just means it did not change locations
									// between requests,
			// could be velocity limit or obstruction etc. group12 - anyone
			// knows what this means?
			boolean blocked = false;

			String[] cardinals = new String[4];
			cardinals[0] = "N";
			cardinals[1] = "E";
			cardinals[2] = "S";
			cardinals[3] = "W";

			String currentDir = cardinals[0];
			/**
			 * #### Rover controller process loop ####
			 */
			while (true) {

				setCurrentLoc();
				previousLoc = currentLoc;

				// ***** do a SCAN ******
				loadScanMapFromSwarmServer();
				scanMap.debugPrintMap();// debug

				// ***** MOVING *****

				// pull the MapTile array out of the ScanMap object
				MapTile[][] scanMapTiles = scanMap.getScanMap();

				int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
				// tile S = y + 1; N = y - 1; E = x + 1; W = x - 1

				List<NextMoveModel> nextMoveNotifier = new ArrayList<NextMoveModel>();

				// ************* G12 - Febi's Smart Logic For motion**********
				// int tempRowArray;
				// int tempColumnArray;

				// logic if going in east
				if (goingEast) {
					// Checks to see if there is science on current tile, if not
					// it moves East
					System.out
							.println("ROVER_12: scanMapTiles[centerIndex][centerIndex].getScience().getSciString() "
									+ scanMapTiles[centerIndex][centerIndex]
											.getScience().getSciString());
					if (scanMapTiles[centerIndex + 1][centerIndex].getScience()
							.equals("C")) {
						// move east
						out.println("MOVE E");
						System.out.println("ROVER_12 request move E");
						goingSouth = false;
						goingEast = true;
						goingNorth = false;
						goingWest = false;
					} else if (scanMapTiles[centerIndex][centerIndex + 1]
							.getScience().equals("C")) {
						// move south
						out.println("MOVE S");
						System.out.println("ROVER_12 request move S");
						goingSouth = true;
						goingEast = false;
						goingNorth = false;
						goingWest = false;
					} else if (scanMapTiles[centerIndex][centerIndex - 1]
							.getScience().equals("C")) {
						// move north
						out.println("MOVE N");
						System.out.println("ROVER_12 request move N");
						goingSouth = false;
						goingEast = false;
						goingNorth = true;
						goingWest = false;
					} else {
						// if next move to east is an obstacle
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
							// check whether south is obstacle
							if (scanMapTiles[centerIndex][centerIndex + 1]
									.getHasRover()
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
									System.out
											.println("ROVER_12 request move W");
									goingSouth = false;
									goingEast = false;
									goingNorth = false;
									goingWest = true;
								} else {
									out.println("MOVE N");
									System.out
											.println("ROVER_12 request move N");
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
				}

				else if (goingWest) {
					// if next move to west is an obstacle
					if (scanMapTiles[centerIndex - 1][centerIndex]
							.getHasRover()
							|| scanMapTiles[centerIndex - 1][centerIndex]
									.getTerrain() == Terrain.ROCK
							|| scanMapTiles[centerIndex - 1][centerIndex]
									.getTerrain() == Terrain.NONE
							|| scanMapTiles[centerIndex - 1][centerIndex]
									.getTerrain() == Terrain.FLUID
							|| scanMapTiles[centerIndex - 1][centerIndex]
									.getTerrain() == Terrain.SAND) {
						// check whether south is obstacle
						if (scanMapTiles[centerIndex][centerIndex + 1]
								.getHasRover()
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

				} else if (goingSouth) {

					// check whether south is obstacle
					if (scanMapTiles[centerIndex][centerIndex + 1]
							.getHasRover()
							|| scanMapTiles[centerIndex][centerIndex + 1]
									.getTerrain() == Terrain.ROCK
							|| scanMapTiles[centerIndex][centerIndex + 1]
									.getTerrain() == Terrain.NONE
							|| scanMapTiles[centerIndex][centerIndex + 1]
									.getTerrain() == Terrain.FLUID
							|| scanMapTiles[centerIndex][centerIndex + 1]
									.getTerrain() == Terrain.SAND) {
						// if next move to west is an obstacle

						if (scanMapTiles[centerIndex - 1][centerIndex]
								.getHasRover()
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
						// if next move to west is an obstacle

						if (scanMapTiles[centerIndex - 1][centerIndex]
								.getHasRover()
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

				}// end of Febi motion
					// #################################
				setCurrentLoc();

				// test for stuckness
				// KS - below causes crash
				// stuck = currentLoc.equals(previousLoc);

				// System.out.println("ROVER_12 stuck test " + stuck);
				System.out.println("ROVER_12 blocked test " + blocked);
				System.out.println(currentLoc);
				pathMap.add(new Coord(currentLoc.getXpos(), currentLoc
						.getYpos()));
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

	} // END of Rover main control loop

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
	// array group12 - this raw JsonData should be used for our maptileLog?
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
		// G12 - Beautiful, Wael!
		myJSONStringBackupofMap = jsonScanMapString;
		
		loadMapTileOntoGlobal(scanMap.getScanMap());
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

	private void loadMapTileOntoGlobal(MapTile[][] ptrScanMap) {

		MapTile tempTile;
		Coord tempCoord;
		Terrain ter;
		Science sci;
		int elev;
		boolean hasR;
		int centerIndex = ptrScanMap.length / 2;

		// debug - print out
		System.out.println("inside of loadMapTileIntoGlobal():");

		for (int i = 0; i < ptrScanMap.length; i++) {
			for (int j = 0; j < ptrScanMap.length; j++) {

				if (withinTheGrid(currentLoc.getYpos() - centerIndex + i,
						currentLoc.getXpos() - centerIndex + j,
						mapTileLog.size())) {
					ter = ptrScanMap[i][j].getTerrain();
					sci = ptrScanMap[i][j].getScience();
					elev = ptrScanMap[i][j].getElevation();
					hasR = ptrScanMap[i][j].getHasRover();

					tempTile = new MapTile(ter, sci, elev, hasR);
					tempCoord = new Coord(currentLoc.getYpos() - centerIndex
							+ i, currentLoc.getXpos() - centerIndex + j);

					mapTileLog.put(tempCoord, tempTile);

					System.out.println(tempCoord + " *** " + tempTile);

				}
			}
		}
		// debug - print out
	}

	private void move(String dir) throws IOException {
		System.out.println("current location in move(): " + currentLoc);
		setCurrentLoc();
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

	// G12 - Nice and neat, Nive!
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
	
	// a check function to prevent IndexOutOfBounds exception
	public boolean withinTheGrid(int i, int j, int arrayLength) {
		return i >= 0 && j >= 0 && i < arrayLength && j < arrayLength;
	}

	/**
	 * Runs the client
	 */
	public static void main(String[] args) throws Exception {
		ROVER_12 client = new ROVER_12();
		client.run();
	}
}
