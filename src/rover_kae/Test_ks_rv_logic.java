package rover_kae;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.DebugGraphics;

import org.junit.Test;

import supportTools.SwarmMapInit;
import common.Coord;
import common.PlanetMap;
import common.RoverLocations;
import common.ScienceLocations;
import controlServer.TestSwarmServer;

public class Test_ks_rv_logic {

	@Test
	public void testCountVisited() throws Exception {
		TestSwarmServer ss = new TestSwarmServer();
		Test_rv rv = new Test_rv();

		ss.runServer("rv12_test.txt");
		
	}
}
