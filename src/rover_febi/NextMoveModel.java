package rover_febi;

public class NextMoveModel {
	int row;
	int column;
	boolean crystal;
	
	
	public NextMoveModel(int row, int column, boolean crystal) {
		super();
		this.row = row;
		this.column = column;
		this.crystal = crystal;
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


	public boolean isCrystal() {
		return crystal;
	}


	public void setCrystal(boolean crystal) {
		this.crystal = crystal;
	}
	
	

	
	
}
