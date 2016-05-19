package supportTools;

import common.Coord;

public class Path {

	private static long idSeed = 0;
	private long id = 0;
	public Coord currCoord;
	public Coord prevCoord;
	public int numVisited;

	public Path() {
	}

	public Path(Coord currCoord, Coord prevCoord) {

		this.currCoord = currCoord;
		this.prevCoord = prevCoord;
		this.numVisited = 1;
		id = idSeed++;
	}

	public String getBackTrackDir() {

		if(prevCoord.equals(currCoord)){
			return "Not moved.";
		}
		if (prevCoord.xpos == currCoord.xpos
				&& prevCoord.ypos < currCoord.ypos) {
			return "N";
		} else if (prevCoord.xpos > currCoord.xpos 
				&& prevCoord.ypos == currCoord.ypos) {
			return "E";
		} else if (prevCoord.xpos == currCoord.xpos
				&& prevCoord.ypos > currCoord.ypos + 1) {
			return "S";
		} else {
			return "W";
		}
	}

	@Override
	public String toString() {
		return "Path [id=" + id + "\n curr=" + currCoord + "\n prev="
				+ prevCoord + "\n numVisited=" + numVisited + "]";
	}
}
