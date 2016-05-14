package rover_kae;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.json.simple.JSONObject;
import org.junit.Test;

import common.Coord;
import common.MapTile;

public class Test_ks {

	@Test
	public void testQuadrantsNullCounter() {

		String[][] array = { { "a", "b", "c" }, { "d", null, "f" },
				{ "g", "h", null }, { "j", "k", "l" } };

		Map<Coord, String> hashmap = new HashMap<Coord, String>();
		for (int j = 0; j < array.length; j++) {
			for (int i = 0; i < array[j].length; i++) {
				hashmap.put(new Coord(i, j), array[j][i]);
			}
		}
		// print hashmap
		for (Map.Entry<Coord, String> num : hashmap.entrySet()) {
			System.out.println("hashmap k,v: " + num.getKey() + ", "
					+ num.getValue());
		}

		int quadrantsHeight = 2, quadrantsWidth = 1;
		Map<Coord, Integer> numNullInQuadrants = new HashMap<Coord, Integer>();
		int tracker = -1, i, j;

		for (j = 0; j < quadrantsHeight * 2; j += quadrantsHeight) {
			tracker = 0;
			for (i = 0; i < quadrantsWidth * 3; i += quadrantsWidth) {
				System.out.println("i,j = " + i + ", " + j);
				if (hashmap.get(new Coord(i, j)) == null) {
					System.out.println("\n\nnull detected!!");
					tracker++;
				}
			}
			numNullInQuadrants.put(new Coord((int) Math.floor(i / 4) * 4,
					(int) (Math.floor(j / 4) * 4)), tracker);
		}

		for (Map.Entry<Coord, Integer> num : numNullInQuadrants.entrySet()) {
			System.out.println("num null quad k,v: " + num.getKey() + ", "
					+ num.getValue());
		}
	}

	// @Test
	public void testgetFurthestQuadrant() {
		ROVER_12_wk8_kae rv = new ROVER_12_wk8_kae();
		Coord q1 = new Coord(1, 1), q2 = new Coord(2, 2), q3 = new Coord(3, 3), q4 = new Coord(
				4, 4);
		System.out.println(rv.getFurthestQuadrant(q1, q2, q3, q4));

	}

	// {"_id":"572e759207cb252a36cfb412","x":11,"y":45,"terrain":"sand","science":"organic","stillExists":true}
	// @Test
	public void testPost() {

		// System.out.println(obj.toString());
		Random rd = new Random();
		MapTile[][] tiles = new MapTile[20][20];
		// for (int i = 0; i < tiles.length; i++) {
		JSONObject obj = new JSONObject();
		obj.put("x", rd.nextInt(21));
		obj.put("y", rd.nextInt(21));
		obj.put("terrain", "sand");
		obj.put("science", "cry");
		obj.put("stillExists", true);
		try {
			sendPost(obj);
			// request(tiles);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// }

	}

	// @Test
	public void testFindMaxIndeces() {

		int[] array = { 4, 6, 2, 9, 1, 17, 2, 17, 5 };
		Set<Integer> maxes = findMaxIndeces(array);
		for (Integer num : maxes) {
			System.out.print(num + " ");
		}

	}

	// @Test
	public void testGetDistanceBetween2Points() {

		System.out.println(getDistanceBetween2Points(new Coord(1, 3),
				new Coord(4, 8)));

	}

	private Set<Integer> findMaxIndeces(int[] array) {
		/*
		 * returns the index/indeces of the element(s) that hold(s) the maximum
		 * value
		 */
		int max = Integer.MIN_VALUE, maxIndex = -1;
		Set<Integer> tie = new HashSet<Integer>();
		for (int i = 0; i < array.length; i++) {
			if (max < array[i]) {
				maxIndex = i;
				max = array[i];
			}
		}
		tie.add(maxIndex);
		/*
		 * if 2 or more quadrant ties, return the farthest from current location
		 * of rover 12
		 */
		for (int i = 0; i < array.length; i++) {
			if (max == array[i]) {
				tie.add(i);
			}
		}
		return tie;
	}

	private double getDistanceBetween2Points(Coord p1, Coord p2) {
		// sqrt((x2-x1)^2+(y2-y1)^2)

		return Math.sqrt(Math.pow(p2.getXpos() - p1.getXpos(), 2)
				+ Math.pow(p2.getYpos() - p1.getYpos(), 2));
	}

	// HTTP POST request
	public void sendPost(JSONObject jsonObj) throws Exception {
		String url = "http://localhost:3000/scout";
		// String url = "http://192.168.0.101:3000/scout";
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

	public String request(MapTile[][] scanMapTile) {

		String USER_AGENT = "ROVER_12";
		// String url = "http://192.168.0.101:3000/globalMap";
		String url = "http://localhost:3000/globalMap";

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

}
