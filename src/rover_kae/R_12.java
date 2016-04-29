package rover_kae;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import common.Coord;
import common.MapTile;
import common.ScanMap;
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
public class R_12 {

	protected BufferedReader in;
	protected PrintWriter out;
	protected String rovername, line;
	protected ScanMap scanMap;
	protected int sleepTime;
	protected String SERVER_ADDRESS = "localhost";
	protected static final int PORT_ADDRESS = 9537;

	protected CoordUtil currentLoc, targetLoc, startLoc;

	public R_12() {
		// constructor
		System.out.println("ROVER_12 rover object constructed");
		rovername = "ROVER_12";
		SERVER_ADDRESS = "localhost";
		// this should be a safe but slow timer value
		sleepTime = 300; // in milliseconds - smaller is faster, but the server
							// will cut connection if it is too small
	}

	public R_12(String serverAddress) {
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
																	// here
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);

		// Gson gson = new GsonBuilder().setPrettyPrinting().create();

		// Process all messages from server, wait until server requests Rover ID
		// name
		while (true) {
			String line = in.readLine();
			if (line.startsWith("SUBMITNAME")) {
				out.println(rovername); // This sets the name of this instance
										// of a swarmBot for identifying the
										// thread to the server
				break;
			}
		}

		// ******** Rover logic *********
		// int cnt=0;
		String line = "";

		boolean goingSouth = false;
		boolean stuck = false; // just means it did not change locations between
								// requests,
								// could be velocity limit or obstruction etc.
		boolean blocked = false;

		String[] cardinals = new String[4];
		cardinals[0] = "N";
		cardinals[1] = "E";
		cardinals[2] = "S";
		cardinals[3] = "W";

		String currentDir = cardinals[0];

		CoordUtil previousLoc = null;

		// Rover crystal storage array
		ArrayList<CoordUtil> RoverStorage = new ArrayList<CoordUtil>();

		// start Rover controller process
		while (true) {

			// currently the requirements allow sensor calls to be made with no
			// simulated resource cost

			// **** location call ****
			currentLoc = getcurrentLoc(currentLoc);
			System.out.println("ROVER_12 currentLoc at start: " + currentLoc);

			// after getting location set previous equal current to be able to
			// check for stuckness and blocked later
			previousLoc = currentLoc;

			// **** get equipment listing ****
			ArrayList<String> equipment = new ArrayList<String>();
			equipment = getEquipment();
			// System.out.println("ROVER_12 equipment list results drive " +
			// equipment.get(0));
			System.out.println("ROVER_12 equipment list results " + equipment
					+ "\n");

			// ***** do a SCAN *****
			// System.out.println("ROVER_12 sending SCAN request");
			this.doScan();
			scanMap.debugPrintMap();

			// ***** MOVING *****
			// try moving east 5 block if blocked
			if (blocked) {
				for (int i = 0; i < 6; i++) {
					out.println("MOVE E");
					System.out.println("ROVER_12 request move E");
					Thread.sleep(300);
				}
				out.println("MOVE E");
				currentDir = cardinals[1];
				blocked = false;
				// reverses direction after being blocked
				goingSouth = !goingSouth;
			} else {

				// pull the MapTile array out of the ScanMap object
				MapTile[][] scanMapTiles = scanMap.getScanMap();
				int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
				// tile S = y + 1; N = y - 1; E = x + 1; W = x - 1

				// Detect current position for existing crystal and collect it
				// if exist

				// to collect anything detected use the below condition
				// !scanMapTiles[centerIndex][centerIndex].getScience().getSciString().equals("N")

				if (scanMapTiles[centerIndex][centerIndex].getScience()
						.getSciString().equals("C")) {
					System.out.println("ROVER_12 request GATHER");
					// Notify server of gathering to update the scanMap
					out.println("GATHER");

					// Get science location which is same as rover
					currentLoc = getcurrentLoc(currentLoc);

					// Add Collected crystals to rover storage
					RoverStorage.add(new CoordUtil(currentLoc.getX(),
							currentLoc.getY()));
					System.out.println("CRYSTAL ADDED TO STORAGE");

				} else {

					if (goingSouth) {
						// check scanMap to see if path is blocked to the south
						// (scanMap may be old data by now)
						if (scanMapTiles[centerIndex][centerIndex + 1]
								.getHasRover()
								|| scanMapTiles[centerIndex][centerIndex + 1]
										.getTerrain() == Terrain.ROCK
								|| scanMapTiles[centerIndex][centerIndex + 1]
										.getTerrain() == Terrain.NONE
								|| scanMapTiles[centerIndex][centerIndex + 1]
										.getTerrain() == Terrain.SAND) {
							blocked = true;
						} else {
							// request to server to move
							out.println("MOVE S");
							currentDir = cardinals[2];
							System.out.println("ROVER_12 request move S");
						}

					} else {

						if (scanMapTiles[centerIndex][centerIndex - 1]
								.getHasRover()
								|| scanMapTiles[centerIndex][centerIndex - 1]
										.getTerrain() == Terrain.ROCK
								|| scanMapTiles[centerIndex][centerIndex - 1]
										.getTerrain() == Terrain.NONE
								|| scanMapTiles[centerIndex][centerIndex - 1]
										.getTerrain() == Terrain.SAND) {
							blocked = true;
						} else {
							// request to server to move
							out.println("MOVE N");
							currentDir = cardinals[0];
							System.out.println("ROVER_12 request move N");
						}
					}
				}
			}

			currentLoc = getcurrentLoc(currentLoc);

			// System.out.println("ROVER_12 currentLoc after recheck: " +
			// currentLoc);
			// System.out.println("ROVER_12 previousLoc: " + previousLoc);

			// test for stuckness
			stuck = currentLoc.equals(previousLoc);

			// System.out.println("ROVER_12 stuck test " + stuck);
			System.out.println("ROVER_12 blocked test " + blocked);

			// TODO - logic to calculate where to move next

			Thread.sleep(sleepTime);

			System.out
					.println("ROVER_12 ------------ bottom process control --------------");
		}

	}

	private CoordUtil getcurrentLoc(CoordUtil currentLoc) throws IOException {
		String line;
		out.println("LOC");
		line = in.readLine();
		if (line == null) {
			System.out.println("ROVER_12 check connection to server");
			line = "";
		}
		if (line.startsWith("LOC")) {
			// loc = line.substring(4);
			currentLoc = extractLOC(line);
		}
		return currentLoc;
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
		// debug print json object to a file
		// new MyWriter( jsonScanMapString, 0); //gives a strange result -
		// prints the \n instead of newline character in the file

		// System.out.println("ROVER_12 convert from json back to ScanMap class");
		// convert from the json string back to a ScanMap object
		scanMap = gson.fromJson(jsonScanMapString, ScanMap.class);
	}

	// this takes the LOC response string, parses out the x and x values and
	// returns a Coord object
	public static CoordUtil extractLOC(String sStr) {
		sStr = sStr.substring(4);
		if (sStr.lastIndexOf(" ") != -1) {
			String xStr = sStr.substring(0, sStr.lastIndexOf(" "));
			// System.out.println("extracted xStr " + xStr);

			String yStr = sStr.substring(sStr.lastIndexOf(" ") + 1);
			// System.out.println("extracted yStr " + yStr);
			return new CoordUtil(Integer.parseInt(xStr), Integer.parseInt(yStr));
		}
		return null;
	}

	private CoordUtil requestTargetLoc() throws IOException {

		// setCurrentLoc(currentLoc);

		out.println("TARGET_LOC " + currentLoc.getX() + " " + currentLoc.getY());
		line = in.readLine();

		if (line == null || line == "") {
			// System.out.println("ROVER_12 check connection to server");
			line = "";
		}

		if (line.startsWith("TARGET")) {
			targetLoc = (CoordUtil) extractTargetLOC(line);
		}
		return targetLoc;
	}

	private CoordUtil requestStartLoc() throws IOException {

		// setCurrentLoc(currentLoc);

		out.println("START_LOC " + currentLoc.getX() + " " + currentLoc.getY());
		line = in.readLine();

		if (line == null || line == "") {
			System.out.println("ROVER_12 check connection to server");
			line = "";
		}

		//
		System.out.println();
		if (line.startsWith("START")) {
			startLoc = (CoordUtil) extractStartLOC(line);
		}
		return startLoc;
	}

	public static CoordUtil extractTargetLOC(String sStr) {
		sStr = sStr.substring(11);
		if (sStr.lastIndexOf(" ") != -1) {
			String xStr = sStr.substring(0, sStr.lastIndexOf(" "));
			// System.out.println("extracted xStr " + xStr);

			String yStr = sStr.substring(sStr.lastIndexOf(" ") + 1);
			// System.out.println("extracted yStr " + yStr);
			return new CoordUtil(Integer.parseInt(xStr), Integer.parseInt(yStr));
		}
		return null;
	}

	public static CoordUtil extractStartLOC(String sStr) {

		sStr = sStr.substring(10);

		if (sStr.lastIndexOf(" ") != -1) {
			String xStr = sStr.substring(0, sStr.lastIndexOf(" "));
			// System.out.println("extracted xStr " + xStr);

			String yStr = sStr.substring(sStr.lastIndexOf(" ") + 1);
			// System.out.println("extracted yStr " + yStr);
			return new CoordUtil(Integer.parseInt(xStr), Integer.parseInt(yStr));
		}
		return null;
	}

	/**
	 * Runs the client
	 */
	public static void main(String[] args) throws Exception {
		R_12 client = new R_12();
		client.run();
	}
}