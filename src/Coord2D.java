import java.util.ArrayList;


public class Coord2D implements Comparable {

	private int x;
	private int y;
	
	public Coord2D(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public Coord2D(float x, float y) {
		this((int) x, (int) y);
	}
	
	public Coord2D(double x, double y) {
		this((int) x, (int) y);
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	
	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}
	
	public void moveX(int dx) {
		this.x += dx;
	}

	public void moveY(int dy) {
		this.y += dy;
	}

	public Coord2D subtract(Coord2D c) {
		x-=c.x;
		y-=c.y;
		return this;
	}
	
	public Coord2D add(Coord2D c) {
		x+=c.x;
		y+=c.y;
		return this;
	}
	
	public static Coord2D sum(Coord2D a, Coord2D b) {
		return new Coord2D(a.x+b.x, a.y+b.y);
	}
	
	public static Coord2D difference(Coord2D a, Coord2D b) {
		return new Coord2D(a.x-b.x, a.y-b.y);
	}
	
	public Coord2D getFraction(double fraction) {
		return new Coord2D((int) (x*fraction), (int) (y*fraction));
	}
	
	public Coord2D shiftTo() {
		//If the x and y coords are equal, return signum(x), signum(y), i.e. the signs of x and y.
		//Otherwise, return signum(x), 0 if x>y, 0, signum(y) otherwise.
		return Math.abs(x)==Math.abs(y) ? new Coord2D(Math.signum(x), Math.signum(y)) :
					  Math.abs(x)>Math.abs(y) ? new Coord2D(Math.signum(x), 0) :
						    new Coord2D(0, Math.signum(y));
	}
	
	public boolean isZero() {
		return (x==0 && y==0);
	}
	
	public String toString() {
		return "("+x+", "+y+")";
	}

	public ArrayList<Coord2D> getOrthogonalNeighbours() {
		ArrayList<Coord2D> neighbours = new ArrayList<Coord2D>();
		neighbours.add(new Coord2D(x-1, y));
		neighbours.add(new Coord2D(x+1, y));
		neighbours.add(new Coord2D(x, y-1));
		neighbours.add(new Coord2D(x, y+1));
		return neighbours;
	}

	@Override
	public int compareTo(Object o) {
		if (!(o instanceof Coord2D)) {
			return (Integer) null;
		} else {
			Coord2D c = (Coord2D) o;
			return c.x + (c.y << 16);
		}
	}
}
