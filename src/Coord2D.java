
public class Coord2D {

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
}
