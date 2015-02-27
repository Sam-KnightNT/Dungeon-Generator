import java.awt.geom.Point2D;
import java.util.*;

import javax.swing.JFrame;


public class Generator {

	/*
	 * Set number of cells to generate (say, 150)
	 * Set a random point in a radius small radius (30 for now, could be ~50)
	 * Set a random size 3<s<20, with a skewed normal distribution (Park-Miller)
	 * Modify the width and height by +-2, minimum should be extended to 1
	 * Draw stuff to screen.
	 * Use separation steering to separate cells
	 * Fill in gaps with 1x1 cells
	 * Any cells above a certain size are rooms (above, say, 6x6)
	 * Use Delaunay Triangulation to connect rooms
	 * Construct a Minimal Spanning Tree
	 * Add a few more edges that were deleted from the Tree (15%)
	 * For each line still existing, construct straight lines or L shapes
	 * Set any cells that intersect these (including larger cells) as corridor tiles
	 */
	
	static final int NUM_CELLS = 150;
	static final int RADIUS_LIMIT = 50;
	static final int MIN_SIZE = 3;
	static final int MAX_SIZE = 20;
	static final double VARIANCE = 3;
	static Random random = new Random();
	static GameWindow window;
	
	static ArrayList<Cell> cells = new ArrayList<Cell>();
	
	public static void main(String[] args) {
		
		//Generate cells
		for (int i = 0; i<NUM_CELLS; i++) {
			
			int size = getRoomParameters();
			int x = random.nextInt(RADIUS_LIMIT*2) - RADIUS_LIMIT;
			int y = random.nextInt(RADIUS_LIMIT*2) - RADIUS_LIMIT;
			
			//int size = random.nextInt(MAX_SIZE-MIN_SIZE) + MIN_SIZE;
			int x_size = random.nextInt(4) - 2 + size;
			int y_size = random.nextInt(4) - 2 + size;
			Cell cell = new Cell(new Coord2D(x, y), x_size, y_size);
			cells.add(cell);	
		}
		
		window = new GameWindow(cells);
		JFrame frame = new JFrame();
		frame.add(window);
		frame.setSize(1000, 1000);
		frame.setVisible(true);
		//printGraphicalOutput(minX, maxX, minY, maxY);

		//Sort the cells by x-value. Check each pair in turn. If they overlap, move the furthest one from the origin 1 square outwards.
		//Sort the cells by y-value. Check each pair in turn. If they overlap, move the furthest one from the origin 1 square outwards.
		//Repeat until there are no overlaps.
		boolean overlaps = true;
		while (overlaps) {
			
			overlaps = false;
			Collections.sort(cells);
			for (Cell cell : cells) {
				
				for (int i=cells.indexOf(cell)+1; i<NUM_CELLS; i++) {
					if (cell.overlaps(cells.get(i))) {
						overlaps = true;
						window.clearCell(cell);
						window.clearCell(cells.get(i));
						cell.moveAwayFrom(cells.get(i));
						cells.get(i).moveAwayFrom(cell);
						window.repaintCell(cell);
						window.repaintCell(cells.get(i));
					}
				}
			}
			
			if (overlaps) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		System.out.println("Success!");

		ArrayList<Cell> oldCells = new ArrayList<Cell>();
		for (Cell cell : cells) {
			oldCells.add((Cell) cell.clone());
		}
		
		//After all this is done, the cells will be massively skewed towards the negative - re-orient them so that the mean is 0 again
		int totalX = 0;
		int totalY = 0;
		for (Cell cell : cells) {
			//Add the x/y coords to the total sum of them 
			totalX += cell.getCentre().getX();
			totalY += cell.getCentre().getY();
		}
		
		//Get the average x/y position (to the nearest integer)
		totalX /= cells.size();
		totalY /= cells.size();
		
		Coord2D averagePosition = new Coord2D(totalX, totalY);
		

		//Transpose the cells to the appropriate place
		for (Cell cell : cells) {
			window.clearCell(cell);
			cell.setCorner(Coord2D.subtract(cell.getCorner(), averagePosition));
			System.out.println(cell.toString());
		}
		
		//Redraw them on-screen
		for (Cell cell : cells) {
			window.repaintCell(cell);
		}
		
		//Now, construct a Delaunay Path and a Minimum Spanning Tree, to get the corridors which should be drawn.
		//Delaunay Triangulation - http://en.wikipedia.org/wiki/Delaunay_triangulation
		//Euclidean MST - http://en.wikipedia.org/wiki/Euclidean_minimum_spanning_tree
		//Alternative - use A* to find paths to other cells.
		//Relative Neighbourhood Graph looks good - http://en.wikipedia.org/wiki/Relative_neighborhood_graph
		//Also see Gabriel Graph http://en.wikipedia.org/wiki/Gabriel_graph and Nearest Neighbour Graph http://en.wikipedia.org/wiki/Nearest_neighbor_graph
		//Then, stick the corridor tiles in the appropriate places.
	}
	
	public static void printGraphicalOutput(int minX, int maxX, int minY, int maxY) {
		boolean[][] cellsPrint = new boolean[maxX-minX][maxY-minY];
		for (Cell cell : cells) {
			Coord2D cor = cell.getCorner();
			for (int y=cor.getY(); y<cor.getY()+cell.getY_size(); y++) {
				for (int x=cor.getX(); x<cor.getX()+cell.getX_size(); x++) {
					cellsPrint[x-minX][y-minY] = true;
				}
			}
		}
		
		for (int y=0; y<maxY-minY; y++) {
			for (int x=0; x<maxX-minX; x++) {
				if (cellsPrint[x][y]) {
					System.out.print(' ');
				} else {
					System.out.print('X');
				}
			}
			System.out.println();
		}
	}
	
	private static int getRoomParameters() {
		double r = 0;
		while (r<MIN_SIZE || r>MAX_SIZE) {
			r = (random.nextGaussian()*VARIANCE)+MIN_SIZE;
		}
		return (int) Math.round(r);
	}
}
