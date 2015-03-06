import java.awt.Color;
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
		
		
		//For now, draw a line from each cell to its nearest neighbour.
		window.getGraphics().setColor(Color.GREEN);
		for (Cell cell : cells) {
			//TODO - add additional check. Only red cells should be considered.
			double closestCellDist = 1000;
			Cell closestCell = null;
			for (Cell otherCell : cells) {
				if (otherCell != cell && cell.getDistanceTo(otherCell)<closestCellDist) {
					closestCell = otherCell;
					closestCellDist = cell.getDistanceTo(otherCell);
				}
			}
			//window.getGraphics().drawLine(500+(cell.getCentre().getX()*5), 500+(cell.getCentre().getY()*5),
			//		500+(closestCell.getCentre().getX()*5), 500+(closestCell.getCentre().getY()*5));
		}
		
		//Create a series of lines between the points, that cover each and every point. Go recursively - check each point.
		//If it doesn't have 2 points from it, find the closest point to it that doesn't have a line to it. Draw a line to that point, and pause for a bit.
		System.out.println("Cells separated: Generating connections.");
		boolean finished = false;
		while (!finished) {
			//return true if all points have been iterated through and none without triangles are found.
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finished = triangulate(); 
		}
		System.out.println("Done!");
		
		//Now check for disconnected loops - generate a list, A, of all cells. While this list is populated, pick the first cell on the list.
		//Generate a list of connected cells, B. Go through this in turn - draw a green line when a connection has been checked.
		//If the cell on the end is still in list A, delete it from A and put its connected cells on the end of list B. Then take the next from list B.
		//Once list B is exhausted, check list A. If it is empty, we are done. If not, check which is larger - A or ~A (i.e. deleted cells).
		//Iterate through the smaller of A and ~A, finding the cell which is the closest to a cell in ~A or A. Draw a line between these 2 cells at the end.
		//Pick the next cell on list A and repeat. If this one empties without returning to the start, it is done.

		finished = false;
		@SuppressWarnings("unchecked")
		ArrayList<Cell> cellGen = (ArrayList<Cell>) cells.clone();
		while (!cellGen.isEmpty()) {
			Cell cell = cellGen.remove(0);
			ArrayList<Cell> connectedCells = cell.getConnections();
			while (!connectedCells.isEmpty()) {
				Cell cCell = connectedCells.remove(0);
				for (Cell ccCell : cCell.getConnections()) {
					if (cellGen.contains(ccCell)) {
						cellGen.remove(ccCell);
						connectedCells.add(ccCell);
					}
				}
			}
			if (!cellGen.isEmpty()) {
				//Then there is a loop not connected, so check which is the smaller loop, go through each cell and find the closest one.
				Cell closestCellA = null;
				Cell closestCellB = null;
				double closestDist = 100;
				ArrayList<Cell> loopCells = (ArrayList<Cell>) cells.clone();
				loopCells.removeAll(cellGen);
				for (Cell loopCell : loopCells) {
					for (Cell otherCell : cellGen) {
						if (loopCell.getDistanceTo(otherCell)<closestDist) {
							closestCellA = loopCell;
							closestCellB = otherCell;
							closestDist = loopCell.getDistanceTo(otherCell);
						}
					}
				}
				//Then connect the two.
				closestCellA.addConnection(closestCellB);
				closestCellB.addConnection(closestCellA);
				
				//And draw the line.
				System.out.println("Connecting closest cells in disjoint loops");
				window.getGraphics().setColor(Color.GREEN);
				window.getGraphics().drawRect(500+(closestCellA.getCentre().getX()*5), 500+(closestCellA.getCentre().getY()*5),
						500+(closestCellB.getCentre().getX()*5), 500+(closestCellB.getCentre().getY()*5));
			}
		}
		
		
	}
	
	public static void printGraphicalOutput(int minX, int maxX, int minY, int maxY) {
		boolean[][] cellsPrint = new boolean[maxX-minX][maxY-minY];
		for (Cell cell : cells) {
			Coord2D cor = cell.getCorner();
			for (int y=cor.getY(); y<cor.getY()+cell.getH(); y++) {
				for (int x=cor.getX(); x<cor.getX()+cell.getW(); x++) {
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
	
	private static boolean triangulate() {
		Collections.shuffle(cells);
		for (Cell cell : cells) {
			if (cell.getConnectionCount() < 2) {
				//Create a new Connection to the closest cell that doesn't already have one.
				double closestCellDist = 1000;
				Cell closestCell = null;
				for (Cell otherCell : cells) {
					if (otherCell != cell && cell.getDistanceTo(otherCell)<closestCellDist && !cell.getConnections().contains(otherCell)) {
						closestCell = otherCell;
						closestCellDist = cell.getDistanceTo(otherCell);
					}
				}
				window.getGraphics().drawLine(500+(cell.getCentre().getX()*5), 500+(cell.getCentre().getY()*5),
						500+(closestCell.getCentre().getX()*5), 500+(closestCell.getCentre().getY()*5));
				
				cell.addConnection(closestCell);
				closestCell.addConnection(cell);
				return false;
			}
		}
		return true;
	}
}
