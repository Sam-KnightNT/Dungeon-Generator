
public class Connection {
	
	//Connects 2 Cells, and that is it.
	Cell cellA;
	Cell cellB;
	
	public Connection(Cell cellA, Cell cellB) {
		this.cellA = cellA;
		this.cellB = cellB;
	}

	public Cell getCellA() {
		return cellA;
	}

	public void setCellA(Cell cellA) {
		this.cellA = cellA;
	}

	public Cell getCellB() {
		return cellB;
	}

	public void setCellB(Cell cellB) {
		this.cellB = cellB;
	}
	
}
