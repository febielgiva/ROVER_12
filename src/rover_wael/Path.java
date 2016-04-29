package rover_wael;

import common.Coord;

public class Path {

	private Coord coord;
	private String Direction;
	
	
	public Path(Coord coord, String direction) {
		super();
		this.coord = coord;
		Direction = direction;
	}
	public Coord getCoord() {
		return coord;
	}
	public void setCoord(Coord coord) {
		this.coord = coord;
	}
	public String getDirection() {
		return Direction;
	}
	public void setDirection(String direction) {
		Direction = direction;
	}
	
}
