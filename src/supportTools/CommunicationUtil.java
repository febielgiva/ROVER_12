package supportTools;

/* Many many thinks to ROVER_11 (Group 11) for their diligent and patient advisement, leadership, and all of the inspirations!!*/
import org.json.simple.JSONObject;

import common.MapTile;
import enums.Science;
import enums.Terrain;

public class CommunicationUtil {
	
	public static MapTile convertToMapTile(JSONObject obj) {
		Terrain terrain = copyTerrain((String) obj.get("terrain"));
		Science science = copyScience((String) obj.get("science"));
		MapTile tile = new MapTile(terrain, science, 0, false);
		return tile;
	}

	public static Terrain copyTerrain(String str) {
		Terrain output;

		switch (str) {
		case "NONE":
			output = Terrain.NONE;
			break;
		case "ROCK":
			output = Terrain.ROCK;
			break;
		case "SOIL":
			output = Terrain.SOIL;
			break;
		case "GRAVEL":
			output = Terrain.GRAVEL;
			break;
		case "SAND":
			output = Terrain.SAND;
			break;
		case "FLUID":
			output = Terrain.FLUID;
			break;

		default:
			output = Terrain.NONE;
		}
		return output;

	}

	public static Science copyScience(String input) {
		Science output;

		switch (input) {
		case "NONE":
			output = Science.NONE;
			break;
		case "RADIOACTIVE":
			output = Science.RADIOACTIVE;
			break;
		case "ORGANIC":
			output = Science.ORGANIC;
			break;
		case "MINERAL":
			output = Science.MINERAL;
			break;
		case "ARTIFACT":
			output = Science.ARTIFACT;
			break;
		case "CRYSTAL":
			output = Science.CRYSTAL;
			break;

		default:
			output = Science.NONE;
		}
		return output;
	}
}
