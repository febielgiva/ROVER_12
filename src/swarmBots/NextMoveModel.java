package swarmBots;

public class NextMoveModel {
	int row;
	int column;
	String dir;
	
	
	public NextMoveModel(int row, int column, String direction) {
		super();
		this.row = row;
		this.column = column;
		this.dir = direction;
	}


	public int getRow() {
		return row;
	}


	public void setRow(int row) {
		this.row = row;
	}


	public int getColumn() {
		return column;
	}


	public void setColumn(int column) {
		this.column = column;
	}


	public String isCrystal() {
		return dir;
	}


	public void setCrystal(String crystal) {
		this.dir = crystal;
	}
	
	

	
	
}
