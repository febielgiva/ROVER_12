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
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import swarmBots.ROVER_12;

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
 * allowed # request to the server per sec = 500 2 req / sec Target Loc: Coord
 * [x=43, y=45] Start Loc: Coord [x=5, y=5] <-- for debug
 */

public class RV_12_ks_current extends ROVER_12 {
	Random rd = new Random();
	CoordUtil currentLoc, previousLoc, startLoc, targetLoc;
	String currentDir = "", line;
	Set<String> blockedDirs = new HashSet<String>();
	Set<String> openDirs = new HashSet<String>();
	String[] cardinals = new String[4];
	MapTile[][] mapJournal = new MapTileUtil[100][100];
	boolean[][] footPrints = new boolean[100][100];

	MapTile[][] tempScanMap;
	int centerIndex = -1, xx = -1, yy = -1;

	public RV_12_ks_current() {
		super();
	}

	public RV_12_ks_current(String serverAddress) {
		super(serverAddress);
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

	public void debugMove() {
		// ***** sand avoidance ******
		// ***** harvest science ******
	}

	public void debugRun() throws IOException, InterruptedException {

		int rdNum;
		String currentDir;
		boolean stuck;

		// TODO - need to close this socket
		makeConnAndInitStream();
		processServerMsgAndWaitForIDRequestCall();
		this.doScan();
		// scanMap.debugPrintMap();

		// ******** Rover logic *********
		String[] cardinals = { "E", "S", "W", "N" };
		String line = "";

		ArrayList<String> equipment = getEquipment();
		System.out.println("ROVER_12 equipment list " + equipment + "\n");

		// for debug
		// moveRover12ToAClearArea();
		setCurrentLoc(currentLoc);

		System.out.println("Current Loc: " + currentLoc);
		System.out.println("Target Loc: " + requestTargetLoc());
		System.out.println("Start Loc: " + requestStartLoc());
		// Thread.sleep(10000);

		// ******** Rover motion *********
		while (true) {
			doScan();
			debugPrint4Dirs(currentLoc);

			if (previousLoc != null && previousLoc.equals(currentLoc)) {
				stuck = true;
			}

			previousLoc = currentLoc;

			doThisWhenStuck_4stepToOpenDir(currentLoc, scanMap.getScanMap());

			// sinusoidal(cardinals);
			int waveLength = 6, waveHeight = 4;

			sinusoidal_RL(cardinals, waveLength, waveHeight);
			sinusoidal_LR(cardinals, waveLength, waveHeight);
			random(cardinals);
			Thread.sleep(sleepTime);

			System.out
					.println("\nROVER_12 ------------ bottom process control --------------");
		}
	}

	public void run() throws IOException, InterruptedException {

		int rdNum;
		String currentDir;
		boolean stuck;

		// TODO - need to close this socket
		makeConnAndInitStream();
		processServerMsgAndWaitForIDRequestCall();
		this.doScan();
		// scanMap.debugPrintMap();

		// ******** Rover logic *********
		String[] cardinals = { "E", "S", "W", "N" };
		String line = "";

		ArrayList<String> equipment = getEquipment();
		System.out.println("ROVER_12 equipment list " + equipment + "\n");

		// for debug
		// moveRover12ToAClearArea();
		setCurrentLoc(currentLoc);

		System.out.println("Current Loc: " + currentLoc);
		System.out.println("Target Loc: " + requestTargetLoc());
		System.out.println("Start Loc: " + requestStartLoc());
		// Thread.sleep(10000);

		// ******** Rover motion *********
		while (true) {
			doScan();
			debugPrint4Dirs(currentLoc);

			if (previousLoc != null && previousLoc.equals(currentLoc)) {
				stuck = true;
			}

			previousLoc = currentLoc;

			doThisWhenStuck_4stepToOpenDir(currentLoc, scanMap.getScanMap());

			// sinusoidal(cardinals);
			int waveLength = 6, waveHeight = 4;

			sinusoidal_RL(cardinals, waveLength, waveHeight);
			sinusoidal_LR(cardinals, waveLength, waveHeight);
			random(cardinals);
			Thread.sleep(sleepTime);

			System.out
					.println("\nROVER_12 ------------ bottom process control --------------");
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
		Socket socket = new Socket(SERVER_ADDRESS, PORT_ADDRESS); // set port
																	// here
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);
	}

	private void processServerMsgAndWaitForIDRequestCall() throws IOException {
		while (true) {
			String line = in.readLine();
			if (line.startsWith("SUBMITNAME")) {
				out.println(rovername);
				break;
			}
		}
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

	// ******* currently developing ****************
	// KSTD - implement
	private boolean isTreasureSpot() {

		return false;
	}

	private boolean isSand(int x, int y) throws IOException {

		if (withinTheGrid(x, y, mapJournal.length)) {

			if (mapJournal[y][x] == null) {
				doScan();
				System.out.print("is (x,y)=(" + x + "," + y + ") sandy? ");
				System.out.println(mapJournal[y][x].getTerrain().equals(
						Terrain.SAND));
				return mapJournal[y][x].getTerrain().equals(Terrain.SAND);
			} else {
				System.out.println("is (x,y)=(" + x + "," + y + ") sandy? "
						+ mapJournal[y][x].getTerrain().equals(Terrain.SAND));
				return mapJournal[y][x].getTerrain().equals(Terrain.SAND);
			}
		}
		return true;
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

	private void findBlockedDirs(Coord currentLoc) {
		int centerIndex = (scanMap.getEdgeSize() - 1) / 2;

		System.out.println("scan map size ( findBlockedDirs() ): "
				+ scanMap.getEdgeSize());

		tempScanMap = scanMap.getScanMap();

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

	}

	// TODO - incomplete
	private void debugPrint4Dirs(Coord currLoc) {
		// System.out.println("center: "+
		// getScanMap().[currLoc.getYpos()][currLoc.getXpos()]);
		scanMap.debugPrintMap();
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

	// TODO - must be test
	private void harvestScience() {
		xx = currentLoc.getX();
		yy = currentLoc.getY();
		if (mapJournal[currentLoc.getX()][currentLoc.getY()] != null
				&& !mapJournal[yy][xx].getScience().getSciString().equals("N")) {
			System.out.println("ROVER_12(98) request GATHER");
			out.println("GATHER");
		}
	}

	private MapTile[][] pullLocalMap() throws IOException {

		MapTile[][] scanMapTiles = scanMap.getScanMap();
		return scanMapTiles;
	}

	private void setCurrentLoc(Coord loc) throws IOException {
		String line;
		out.println("LOC");
		line = in.readLine();
		if (line == null) {
			// System.out.println("ROVER_12 check connection to server");
			line = "";
		}
		if (line.startsWith("LOC")) {
			// loc = line.substring(4);
			currentLoc = (CoordUtil) extractCurrLOC(line);
		}
	}

	private void snake(String[] cardinals, int scanRange) {
		// TODO Auto-generated method stub

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
				Thread.sleep(700);
			}
		}
	}

	private boolean isStuck(Coord curr, Coord prev) {
		return curr.equals(prev);
	}

	// TODO - must be implemented
	private void awayFromSand() {
	}

	private void sinusoidal_LR(String[] cardinals, int waveLength,
			int waveHeight) throws InterruptedException, IOException {
		int steps;

		steps = waveLength;
		String currentDir;
		cardinals[0] = "E";
		cardinals[1] = "S";
		cardinals[2] = "E";
		cardinals[3] = "N";

		try {
			setCurrentLoc(currentLoc);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (previousLoc != null && isStuck(currentLoc, previousLoc)) {
			doThisWhenStuck_4stepToOpenDir(currentLoc, scanMap.getScanMap());
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
				move(currentDir);
				Thread.sleep(700);
			}
		}
	}

	private void move(String dir) throws IOException {
		System.out.println("current location in move(): " + currentLoc);
		CoordUtil loc = currentLoc.clone();

		switch (dir) {

		case "E":
			if (!isSand(loc.getX() + 1, loc.getY()))
				moveEast();
			break;
		case "W":
			if (!isSand(loc.getX() - 1, loc.getY()))
				moveWest();
			break;
		case "N":
			if (!isSand(loc.getX(), loc.getY() - 1))
				moveNorth();
			break;
		case "S":
			if (!isSand(loc.getX(), loc.getY() + 1))
				moveSouth();
			break;
		default:
			break;
		}
	}

	private void moveEast() {
		out.println("MOVE E");
		System.out.print(currentLoc + " - E -> ");
		currentLoc.incrementX();
		harvestScience();
		System.out.print(currentLoc + "\n");
		System.out.println("mapJournal debug test: "
				+ mapJournal[currentLoc.getX()][currentLoc.getY()]);

		// debug
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		footPrints[currentLoc.getY()][currentLoc.getX()] = true;
	}

	private void moveWest() {
		out.println("MOVE W");
		System.out.print(currentLoc + " - W -> ");
		currentLoc.decrementX();
		harvestScience();
		System.out.print(currentLoc + "\n");
		footPrints[currentLoc.getY()][currentLoc.getX()] = true;
	}

	private void moveNorth() {
		out.println("MOVE N");
		System.out.print(currentLoc + " - N -> ");
		currentLoc.decrementY();
		harvestScience();
		System.out.print(currentLoc + "\n");
		footPrints[currentLoc.getY()][currentLoc.getX()] = true;
	}

	private void moveSouth() {
		out.println("MOVE S");
		System.out.print(currentLoc + " - S -> ");
		currentLoc.incrementY();
		harvestScience();
		System.out.print(currentLoc + "\n");
		footPrints[currentLoc.getY()][currentLoc.getX()] = true;
	}

	private void sinusoidal_RL(String[] cardinals, int waveLength,
			int waveHeight) throws InterruptedException, IOException {
		int steps;

		steps = waveLength;
		String currentDir;
		cardinals[0] = "W";
		cardinals[1] = "S";
		cardinals[2] = "W";
		cardinals[3] = "N";

		try {
			setCurrentLoc(currentLoc);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (previousLoc != null && isStuck(currentLoc, previousLoc)) {
			doThisWhenStuck_4stepToOpenDir(currentLoc, scanMap.getScanMap());
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
				move(currentDir);
				Thread.sleep(700);
			}
		}
	}

	private void moveTowardsSandForDebug() {

	}

	private void random(String[] cardinals) throws InterruptedException,
			IOException {
		int rdNum;
		String currentDir;
		for (int i = 0; i < 5; i++) {
			rdNum = randomNum(0, 3);
			currentDir = cardinals[rdNum];

			for (int j = 0; j < 3; j++) {
				move(currentDir);
				Thread.sleep(300);
			}
		}
	}

	private void moveRover12ToAClearArea() throws InterruptedException,
			IOException {
		for (int i = 0; i < 5; i++) {
			move("E");
			Thread.sleep(700);
		}
		for (int i = 0; i < 5; i++) {
			// get out of the crowd of rovers
			move("S");
			Thread.sleep(700);
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

	public void initMapJournal() {
		for (int i = 0; i < mapJournal.length; i++) {
			for (int j = 0; j < mapJournal[i].length; j++) {

			}
		}
	}

	// sends a SCAN request to the server and puts the result in the scanMap
	// array
	public void doScan() throws IOException {
		System.out.println("ROVER_12 method doScan()");
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		out.println("SCAN");

		// grabs the string that was returned first
		String jsonScanMapIn = in.readLine();

		if (jsonScanMapIn == null) {
			System.out.println("ROVER_12 check connection to server");
			jsonScanMapIn = "";
		}
		StringBuilder jsonScanMap = new StringBuilder();
		System.out.println("ROVER_12 incomming SCAN result - first readline: "
				+ jsonScanMapIn);

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
		int scanRange = 11;

		Terrain ter;
		Science sci;
		int elev;
		boolean hasR;

		int scanMapHalfSize = (int) Math.floor(ptrScanMap.length / 2.);

		setCurrentLoc(currentLoc);

		// set top left corner of the section of the map on the global map
		// journal
		Coord start = new Coord(currentLoc.getXpos() - scanMapHalfSize,
				currentLoc.getYpos() - scanMapHalfSize);

		// debug
		System.out.println("scanMap: ");
		debugPrintMapTileArray(ptrScanMap);

		// FIXME - must correctly record scanned area of the map from scanMaps
		// to mapJournal
		for (int i = 0; i < ptrScanMap.length; i++) {
			for (int j = 0; j < ptrScanMap.length; j++) {
				System.out.println("current loc: " + currentLoc + "\t(i,j)="
						+ "(" + i + "," + j
						+ ")\t[start.ypos + i][start.xpos + j]=" + "["
						+ (start.ypos + i) + "][" + (start.xpos + j) + "]");
				if (withinTheGrid(start.ypos + i, start.xpos + j,
						mapJournal.length)
						&& mapJournal[start.ypos + i][start.xpos + j] == null) {
					ter = ptrScanMap[i][j].getTerrain();
					sci = ptrScanMap[i][j].getScience();
					elev = ptrScanMap[i][j].getElevation();
					hasR = ptrScanMap[i][j].getHasRover();
					System.out.println("************"+ter+" "+sci+" "+elev+" "+hasR);

					mapJournal[start.ypos + i][start.xpos + j] = new MapTileUtil(
							ter, sci, elev, hasR);
					System.out.println("scanMap:"
							+ ptrScanMap[i][j].getTerrain() + " "
							+ ptrScanMap[i][j].getScience() + " "
							+ ptrScanMap[i][j].getHasRover());
					System.out.println("map journal:"
//							+ mapJournal[start.ypos + i][start.ypos + j]
//									.getTerrain()
							+ " "
							+ mapJournal[start.ypos + i][start.ypos + j]
									//.getScience()
//							+ " "
//							+ mapJournal[start.ypos + i][start.ypos + j]
//									.getHasRover()
									);
				}
			}
		}

		debugPrintMapTileArray(mapJournal);

		// debug
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean withinTheGrid(int i, int j, int arrayLength) {
		return i >= 0 && j >= 0 && i < arrayLength && j < arrayLength;
	}

	public void printMapTileArray(MapTile[][] array) {
		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array[i].length; j++) {
				System.out.print(array[i][j] + " ");
			}
			System.out.println();
		}
	}

	public void printMapJournal() {
		System.out.println("current map journal status:");
		for (int i = 0; i < mapJournal.length; i++) {
			for (int j = 0; j < mapJournal.length; j++) {

				System.out.print(mapJournal[i][j].getTerrain() + "("
						+ mapJournal[i][j].getScience() + ")" + " ");
			}
			System.out.println();
		}
	}

	// this takes the LOC response string, parses out the x and y values and
	// returns a Coord object
	public static CoordUtil extractCurrLOC(String sStr) {
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

	public int randomNum(int min, int max) {
		return rd.nextInt(max + 1) + min;
	}

	/**
	 * Runs the client
	 */
	public static void main(String[] args) throws Exception {
		RV_12_ks_current client = new RV_12_ks_current();
		// client.run();
		client.debugRun();
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
}