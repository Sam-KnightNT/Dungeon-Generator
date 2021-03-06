import java.util.ArrayList;


public class Cell implements Comparable<Cell> {

	private Coord2D corner;
	private Coord2D centre;
	private int width;
	private int height;
	private ArrayList<Cell> connections = new ArrayList<Cell>();
	
	public Cell(Coord2D corner, int x, int y) {
		this.corner = corner;
		width = x;
		height = y;
		centre = new Coord2D(corner.getX()+(int) Math.floor(width/2), corner.getY()+(int) Math.floor(height/2));
	}
	
	public Coord2D getCentre() {
		return centre;
	}
	
	public Coord2D getCorner() {
		return corner;
	}

	public int getX() {
		return corner.getX();
	}
	
	public int getY() {
		return corner.getY();
	};
	
	public int getW() {
		return width;
	}

	public int getH() {
		return height;
	}

	public void setCorner(Coord2D corner) {
		this.corner = corner;
		centre = new Coord2D(corner.getX()+(int) Math.floor(width/2), corner.getY()+(int) Math.floor(height/2));
	}

	public void setCentre(Coord2D centre) {
		this.centre = centre;
	}
	
	public void setX_size(int x_size) {
		this.width = x_size;
	}

	public void setY_size(int y_size) {
		this.height = y_size;
	}
	
	public String toString() {
		String s = String.format("Cell of size %3d, %3d. Corner %3d, %3d.", width, height, corner.getX(), corner.getY());
		return s;
	}

	public int compareTo(Cell c) {
		if (corner.getX()!=c.corner.getX()) {
			return (int) (corner.getX()-c.corner.getX());
		} else if (corner.getY()!=c.corner.getY()) {
			return (int) (corner.getY()-c.corner.getY());
		} else {
			return (width*height)-(c.width*c.height);
		}
	}
	
	public boolean overlaps(Cell c) {
		int x1 = corner.getX();
		int x2 = c.corner.getX();
		int y1 = corner.getY();
		int y2 = c.corner.getY();
		
		//Horizontal and vertical collisions. Iff both are true, the cells overlap.
		boolean horz = false;
		boolean vert = false;

		//If this is further right than the other Cell, check that one's size.
		if (x1>x2) {
			horz = x2+c.width >= x1;
		} else {
			horz = x1+width >= x2;
		}
		
		//If this is below the other cell, check that one's size.
		if (y1>y2) {
			vert = y2+c.height >= y1;
		} else {
			vert = y1+height >= y2;
		}
		
		//Return true iff horz and vert collisions.
		return horz && vert;
	}
	
	public void move(int xDiff, int yDiff) {
		corner = new Coord2D(getX()+xDiff, getY()+yDiff);
	}
	
	public void moveAwayFrom(Cell cell) {
		int xDiff = cell.getX()-getX();
		int yDiff = cell.getY()-getY();
		//Move directly away from the centre, far enough so that the cells do not touch
		setCorner(new Coord2D(getX()-(int) Math.signum(xDiff), getY()-(int) Math.signum(yDiff)));
		
		//If xDiff and yDiff happen to be 0, move 1 square to the right
		if (xDiff==0 && yDiff==0) {
			setCorner(new Coord2D(getX()+1, getY()));
		}
	}
	
	public Cell clone() {
		return new Cell(new Coord2D(getX(), getY()), width, height); 
	}
	
	public double getDistanceTo(Cell cell) {
		int xDist = centre.getX()-cell.centre.getX();
		int yDist = centre.getY()-cell.centre.getY();
		return Math.sqrt((xDist*xDist)+(yDist*yDist));
	}
	
	public void addConnection(Cell cell) {
		connections.add(cell);
	}
	
	public ArrayList<Cell> getConnections() {
		return connections;
	}
	
	public int getConnectionCount() {
		return connections.size();
	}
}
