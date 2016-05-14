package common;

import enums.RoverName;
import enums.Science;
import enums.Terrain;

public class MapTile {
	protected Terrain terrain;
	protected int elevation; // not currently used
	protected Science science; // for use on ScanMap, not used on PlanetMap
	protected boolean hasRover; // for use on ScanMap, not used on PlanetMap

	public MapTile() {
		terrain = Terrain.SOIL;
		science = Science.NONE;
		elevation = 0;
		hasRover = false;
	}

	public MapTile(int notUsed) {
		// use any integer as an argument to create MapTile with no terrain
		terrain = Terrain.NONE;
		science = Science.NONE;
		elevation = 0;
		hasRover = false;
	}

	public MapTile(String terrainLetter) {
		// use any String as an argument to create MapTile with no terrain
		this.terrain = Terrain.getEnum(terrainLetter);

		this.science = Science.NONE;
		this.elevation = 0;
		this.hasRover = false;
	}

	public MapTile(Terrain ter, int elev) {
		this.terrain = ter;
		this.science = Science.NONE;
		this.elevation = elev;
		this.hasRover = false;
	}

	public MapTile(Terrain ter, Science sci, int elev, boolean hasR) {
		this.terrain = ter;
		this.science = sci;
		this.elevation = elev;
		this.hasRover = hasR;
	}

	public MapTile getCopyOfMapTile() {
		MapTile rTile = new MapTile(this.terrain, this.science, this.elevation,
				this.hasRover);
		return rTile;
	}

	@Override
	public boolean equals(Object o) {
		MapTile other = (MapTile) o;
		if (other.terrain.equals(terrain) && other.science.equals(science)
				&& other.elevation == elevation && other.hasRover == hasRover) {
			return true;
		}
		return false;
	}

	// No setters in this class to make it thread safe

	public Terrain getTerrain() {
		return this.terrain;
	}

	public Science getScience() {
		return this.science;
	}

	public int getElevation() {
		return this.elevation;
	}

	public boolean getHasRover() {
		return this.hasRover;
	}

	//G12 - Does anyone know what this means?
	// well, broke the thread safe rule
	public void setHasRoverTrue() {
		this.hasRover = true;
	}

	public void setSciecne(Science sci) {
		this.science = sci;
	}

	@Override
	public String toString() {
		return "MapTile [terrain=" + terrain + ", elevation=" + elevation
				+ ", science=" + science + ", hasRover=" + hasRover + "]";
	}
}
