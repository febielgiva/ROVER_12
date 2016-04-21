package swarmBots;

/* Richard's Repo: 
 * 		https://github.com/CS-537-Spring-2016/ROVER_12.git
 Forked Team Repo:
 https://github.com/ks1k1/ROVER_12.git
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
 */

public class ROVER_12_Kae_sinusoidal {

	BufferedReader in;
	PrintWriter out;
	String rovername;
	ScanMap scanMap;
	int sleepTime;
	String SERVER_ADDRESS = "localhost";
	static final int PORT_ADDRESS = 9537;
	Random rd = new Random();

	public ROVER_12_Kae_sinusoidal() {
		// constructor
		System.out.println("ROVER_12 rover object constructed");
		rovername = "ROVER_12";
		SERVER_ADDRESS = "localhost";
		// this should be a safe but slow timer value
		sleepTime = 300; // in milliseconds - smaller is faster, but the server
							// will cut connection if it is too small
	}

	public ROVER_12_Kae_sinusoidal(String serverAddress) {
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
		int rdNum;
		String currentDir;
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
			System.out.println("DBG line 82 = " + line);
			if (line.startsWith("SUBMITNAME")) {
				out.println(rovername); // This sets the name of this instance
										// of a swarmBot for identifying the
										// thread to the server
				break;
			}
		}

		// ******** Rover logic *********
		// int cnt=0;
		String line = "", currectDir = "";

		boolean stuck = false;
		boolean blocked = false;

		String[] cardinals = new String[4];
		cardinals[0] = "E";
		cardinals[1] = "S";
		cardinals[2] = "E";
		cardinals[3] = "N";

		Coord currentLoc = null;
		Coord previousLoc = null;

		moveRover12ToAClearArea();

		// start Rover controller process
		while (true) {

			// ***** ROVER MOTION *****
			
			snake(cardinals,1);
			
			// sinusoidal(cardinals);
			// sinusoidal(cardinals, 2, 4);
			// random(cardinals);
			Thread.sleep(sleepTime);

			System.out
					.println("ROVER_12 ------------ bottom process control --------------");
		}
	}

	private void snake(String[] cardinals, int scanRange) {
		// TODO Auto-generated method stub
		
	}

	private void sinusoidal(String[] cardinals) throws InterruptedException {

		int waveLength = 3, waveHeight = 6, steps = waveLength;
		String currentDir;

		for (int i = 0; i < cardinals.length; i++) {

			currentDir = cardinals[i];
			if (currentDir.equals("E") || currentDir.equals("E")) {
				steps = waveLength;
			} else {
				steps = waveHeight;
			}

			for (int j = 0; j < steps; j++) {
				out.println("MOVE " + currentDir);	
				Thread.sleep(700);
			}
		}
	}
	
	private void sinusoidal(String[] cardinals, int waveLength, int waveHeight) throws InterruptedException {
		int steps;
		
		steps = waveLength;
		String currentDir;

		for (int i = 0; i < cardinals.length; i++) {

			currentDir = cardinals[i];
			if (currentDir.equals("E") || currentDir.equals("E")) {
				steps = waveLength;
			} else {
				steps = waveHeight;
			}

			for (int j = 0; j < steps; j++) {
				out.println("MOVE " + currentDir);	
				Thread.sleep(700);
			}
		}
	}

	private void random(String[] cardinals) throws InterruptedException {
		int rdNum;
		String currentDir;
		for (int i = 0; i < 5; i++) {
			rdNum = randomNum(0, 3);
			currentDir = cardinals[rdNum];

			for (int j = 0; j < 3; j++) {
				out.println("MOVE " + currentDir);
				System.out.println("## move " + currentDir + " [" + rdNum
						+ "] ##");
				Thread.sleep(800);
			}
		}
	}

	private void moveRover12ToAClearArea() throws InterruptedException {
		for (int i = 0; i < 5; i++) {
			out.println("MOVE E");
			Thread.sleep(700);
		}
		for (int i = 0; i < 5; i++) {
			// get out of the crowd of rovers
			out.println("MOVE S");
			Thread.sleep(700);
		}
	}

	// ################ Support Methods ###########################

	private void clearReadLineBuffer() throws IOException {
		while (in.ready()) {
			System.out.println("ROVER_12 clearing readLine()");
			String garbage = in.readLine();
		}
	}

	// method to retrieve a list of the rover's equipment from the server
	private ArrayList<String> getEquipment() throws IOException {
		System.out.println("ROVER_12 method getEquipment()");
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		out.println("EQUIPMENT");

		String jsonEqListIn = in.readLine(); // grabs the string that was
												// returned first
		if (jsonEqListIn == null) {
			jsonEqListIn = "";
		}
		StringBuilder jsonEqList = new StringBuilder();
		System.out
				.println("ROVER_12 incomming EQUIPMENT result - first readline: "
						+ jsonEqListIn);

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
		System.out.println("DBG jsonScanMapIn 336 = " + jsonScanMapIn);

		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

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

	// this takes the LOC response string, parses out the x and y values and
	// returns a Coord object
	public static Coord extractLOC(String sStr) {
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

	public int randomNum(int min, int max) {
		return rd.nextInt(max + 1) + min;
	}

	// one of the motion dictating method (will be moved and adjusted to the
	// appropriate location)
	public void zigzagMotion(double[][] dct, int block_size, int channel) {

		double[][] temp_dct = new double[block_size][block_size];

		for (int i = 0; i < dct.length; i += 8) {
			for (int j = 0; j < dct[i].length; j += 8) {

				for (int i1 = 0; i1 < dct.length; i1++) {
					for (int j1 = 0; j1 < dct[i1].length; j1++) {
						temp_dct[i1][j1] = dct[i][j];
					}
				}

				// for ( CodeRunLengthPair p : temp_i_rep ) {
				// intermediate_rep.add( p );
				// }
			}
		}
	}

	/**
	 * Runs the client
	 */
	public static void main(String[] args) throws Exception {
		ROVER_12_Kae_sinusoidal client = new ROVER_12_Kae_sinusoidal();
		client.run();
	}
}