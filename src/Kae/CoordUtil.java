package Kae;

import common.Coord;

public class CoordUtil extends Coord {
	int x, y;

	public CoordUtil(int x, int y) {
		super(x, y);
		this.x = x;
		this.y = y;
	}

	public void incrementX(CoordUtil loc) {
		loc.x = x++;
	}

	public void incrementY(CoordUtil loc) {
		loc.y = y++;
	}

	public void decrementX(CoordUtil loc) {
		loc.x = x--;
	}

	public void decrementY(CoordUtil loc) {
		loc.y = y--;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public CoordUtil clone() {
		return new CoordUtil(x, y);
	}
	@Override
	public String toString() {
		return "Coord [xpos=" + x + ", ypos=" + y + "]";
	}
}
