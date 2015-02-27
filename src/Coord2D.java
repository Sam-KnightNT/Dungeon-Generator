
public class Coord2D {

	private int x;
	private int y;
	
	public Coord2D(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	
	public static Coord2D subtract(Coord2D c1, Coord2D c2) {
		return new Coord2D(c1.getX()-c2.getX(), c1.getY()-c2.getY());
	}
	
	public static Coord2D add(Coord2D c1, Coord2D c2) {
		return new Coord2D(c1.getX()+c2.getX(), c1.getY()+c2.getY());
	}
	
}
