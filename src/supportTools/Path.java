package supportTools;

import common.Coord;

public class Path {
	private Coord coord;
	private String direction;
	private String[] openPaths;
	private boolean n, e, s, w;

	public Path(Coord coord, String direction, String[] openPaths, boolean n,
			boolean e, boolean s, boolean w) {
		super();
		this.coord = coord;
		this.direction = direction;
		this.openPaths = openPaths;
		this.n = n;
		this.e = e;
		this.s = s;
		this.w = w;
	}

	public Coord getCoord() {
		return coord;
	}

	public void setCoord(Coord coord) {
		this.coord = coord;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public String[] getOpenPaths() {
		return openPaths;
	}

	public void setOpenPaths(String[] openPaths) {
		this.openPaths = openPaths;
	}

	public boolean isN() {
		return n;
	}

	public void setN(boolean n) {
		this.n = n;
	}

	public boolean isE() {
		return e;
	}

	public void setE(boolean e) {
		this.e = e;
	}

	public boolean isS() {
		return s;
	}

	public void setS(boolean s) {
		this.s = s;
	}

	public boolean isW() {
		return w;
	}

	public void setW(boolean w) {
		this.w = w;
	}

}
