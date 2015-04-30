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
	
	/*
	 * Alternative method
	 * Create a number of very large cells, and separation steer them, but only while they are strictly overlapping.
	 * For each cell:
	 * -Get all cells that are directly connected to this one.
	 * -For each one of these:
	 * --Select a random position for a Corridor.
	 * --Progressively shave lines off each, leaving the Corridor intact, until it gets to some length
	 */
	
	/*
	 * Alternative method
	 * Do it like D&D does
	 * Have a large number of pre-set Room types, which are defined in terms of probabilities and put in Slots in a list.
	 * For example, Slot 8 could be a Throne Room, which is 9x12 and has pillars, with a Gold throne in the centre and 3 entrances at set locations.
	 * Once this is generated, roll to determine which Room or Corridor is beyond these, if any. For example, the Throne Room's first door could have attributes
	 * {0, 0, 0, 1, 2, 2, 4, 4, 4, 4, 4, 4, 4, 4, 6, 6, 6, 6, 6, 6, 6, 9, 9, 9, 9}
	 * Roll a die from 1 to size(thatarray).
	 * If it is 01 to 03, nothing is created - the door does not exist.
	 * If it is 04, put Room 1 behind it - this is a Treasure room, with an invisible door that needs to be searched carefully for.
	 * 05 or 06 -> Room 2, trap treasure room - identical to the other one, but with traps instead of treasure.
	 * 07 to 14 -> Room 4, a Corridor that is 5 long and 2 wide, with a door on the left on the 3rd space, and a space for another Corridor at the end.
	 * 15 to 21 -> Room 6, a Corridor that is 8 long and 3 wide, where the 3rd row is trapped.
	 * 22 to 25 -> Room 9, a trap-filled Corridor that is guaranteed to be a treasure room at the end of it.
	 * Define all entrances in this way, as well as any potential rewards. E.g. in the treasure room there are 3 treasure spots - {1, 4, 4, 4, 4, 5}, {4, 4, 4, 5, 6, 7, 7}, {4, 7, 7, 8, 9, 10, 11}.
	 * Each time one of these Rooms is generated it may alter its own chances in other Rooms.
	 * For example, if that entrance above generates any room, add another 0 chance to that entrance's list. If it's a treasure room, add 1 to all trap treasure options. If it's a trap treasure room, add 1 to all treasure rooms.
	 * After the first Throne Room, change slot 8 to have a 1/8 chance of being another Throne Room (possibly modified to be smaller), and 7/8 to be a more generic Room. If this 2nd Throne Room is genned, reduce that chance to 0.
	 * Make sure the new Room can be placed at each stage. If not, remove it from this particular entrance's pool and try again.
	 */
	static final int NUM_CELLS = 4;
	static final int RADIUS_LIMIT_X = 70;
	static final int RADIUS_LIMIT_Y = 50;
	static final int MIN_SIZE = 3;
	static final int MAX_SIZE = 20;
	static final int SKEW = 1;
	static final double VARIANCE = 3;
	static final int CENTREX = 500;
	static final int CENTREY = 375;
	static final int SIZE_MULT = 14;
	static final double TAU = Math.PI*2;
	static Random random = new Random(3000);
	static GameWindow window;
	
	static ArrayList<Cell> cells = new ArrayList<Cell>();
	static ArrayList<Cell> corridors = new ArrayList<Cell>();
	static ArrayList<Connection> connections = new ArrayList<Connection>();
	
	public static void main(String[] args) {
		//Generate cells
		for (int i = 0; i<NUM_CELLS; i++) {
			
			int size = getRoomParameters();
			int x = random.nextInt(RADIUS_LIMIT_X*2) - RADIUS_LIMIT_X;
			int y = random.nextInt(RADIUS_LIMIT_Y*2) - RADIUS_LIMIT_Y;
			
			//int size = random.nextInt(MAX_SIZE-MIN_SIZE) + MIN_SIZE;
			int x_size = random.nextInt(2)*2 - 2 + size;
			int y_size = random.nextInt(2)*2 - 2 + size;
			Cell cell = new Cell(new Coord2D(x, y), x_size, y_size);
			cells.add(cell);	
		}
		
		cells.clear();
		cells.add(new Cell(new Coord2D(-15, -12), 10, 10));
		cells.add(new Cell(new Coord2D(15, -12), 10, 10));
		cells.add(new Cell(new Coord2D(-15, 12), 10, 10));
		cells.add(new Cell(new Coord2D(15, 12), 10, 10));
		
		window = new GameWindow();
		JFrame frame = new JFrame();
		frame.add(window);
		frame.setSize(CENTREX*2, CENTREY*2);
		frame.setVisible(true);
		window.setGraphics();
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
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		System.out.println("Success!");
		
		//Delete any blue cells, i.e. ones which are less than 20 cells in size.
		ArrayList<Integer> cellsToDelete = new ArrayList<Integer>();
		for (int i=0; i<cells.size(); i++) {
			Cell cell = cells.get(i);
			if (cell.getH()*cell.getW()<=20) {
				cellsToDelete.add(i);
			}
		}
		
		for (int i=cellsToDelete.size()-1; i>=0; i--) {
			System.out.println("i: "+i);
			window.clearCell(cells.get(cellsToDelete.get(i)));
			System.out.println(cellsToDelete.get(i)+", "+cells.get(cellsToDelete.get(i)).toStringShort());
			cells.remove((int) cellsToDelete.get(i));
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
		
		window.slowTranspose(cells, averagePosition);
		
		//Now, construct a Delaunay Path and a Minimum Spanning Tree, to get the corridors which should be drawn.
		//Delaunay Triangulation - http://en.wikipedia.org/wiki/Delaunay_triangulation
		//Euclidean MST - http://en.wikipedia.org/wiki/Euclidean_minimum_spanning_tree
		//Alternative - use A* to find paths to other cells.
		//Relative Neighbourhood Graph looks good - http://en.wikipedia.org/wiki/Relative_neighborhood_graph
		//Also see Gabriel Graph http://en.wikipedia.org/wiki/Gabriel_graph and Nearest Neighbour Graph http://en.wikipedia.org/wiki/Nearest_neighbor_graph
		//Then, stick the corridor tiles in the appropriate places.
		
		
		//For now, draw a line from each cell to its nearest neighbour.
		window.getGraphics().setColor(Color.GREEN);
		
		//Create a series of lines between the points, that cover each and every point. Go recursively - check each point.
		//If it doesn't have 2 points from it, find the closest point to it that doesn't have a line to it. Draw a line to that point, and pause for a bit.
		System.out.println("Cells separated: Generating connections.");
		boolean finished = false;
		while (!finished) {
			//return true if all points have been iterated through and none without triangles are found.
			try {
				Thread.sleep(25);
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
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//While cellGen still has cells in it...
		while (!cellGen.isEmpty()) {
			//Take the first one
			Cell cell = cellGen.remove(0);
			
			//Get all of its connections.
			ArrayList<Cell> connectedCells = cell.getConnections();
			
			//While there are connections in this list...
			while (!connectedCells.isEmpty()) {
				//Take the first one in the list.
				Cell cCell = connectedCells.remove(0);
				window.drawCellPart(Color.GREEN, cCell, 5);
				//For each of its connections...
				for (Cell ccCell : cCell.getConnections()) {
					//If the full list contains the connected cell...
					if (cellGen.contains(ccCell)) {
						//Remove it.
						cellGen.remove(ccCell);
						//Draw a green square, to indicate the cell has been reached.
						window.drawCellPart(Color.GREEN, ccCell, 3);
						connectedCells.add(ccCell);
						try {
							Thread.sleep(25);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
			for (Cell cellP : cells) {
				window.repaintCell(cellP);
			}
			if (!cellGen.isEmpty()) {
				//Then there is a loop not connected, so check which is the smaller loop, go through each cell and find the closest one.
				Cell closestCellA = null;
				Cell closestCellB = null;
				double closestDist = 100;
				Collections.sort(cells);
				Collections.sort(cellGen);
				boolean changed = cells.removeAll(cellGen);
				System.out.println(changed);
				System.out.print("1st group");
				for (Cell cell1 : cells) {
					System.out.print(": "+cell1.toStringShort());
				}
				System.out.print("\n2nd group");
				for (Cell cell1 : cellGen) {
					System.out.print(": "+cell1.toStringShort());
				}
				System.out.println();
				for (Cell loopCell : cells) {
					for (Cell otherCell : cellGen) {
						if (loopCell.getDistanceTo(otherCell)<closestDist) {
							closestCellA = loopCell;
							closestCellB = otherCell;
							closestDist = loopCell.getDistanceTo(otherCell);
						}
					}
				}
				cells.addAll(cellGen);
				//Then connect the two.
				closestCellA.addConnection(closestCellB);
				closestCellB.addConnection(closestCellA);
				connections.add(new Connection(closestCellA, closestCellB));
				
				for (Cell cell1 : cells) {
					for (Cell cell2 : cell1.getConnections()) {
						System.out.println("Connection between "+cell1.toStringShort()+" and "+cell2.toStringShort());
					}
				}
				//And draw the line.
				System.out.println("Connecting closest cells in disjoint loops. Cells are "+closestCellA.getCentre()+" and "+closestCellB.getCentre());
				window.getGraphics().setColor(Color.WHITE);
				window.getGraphics().drawLine(CENTREX+(closestCellA.getCentre().getX()*5), CENTREY+(closestCellA.getCentre().getY()*5),
						CENTREX+(closestCellB.getCentre().getX()*5), CENTREY+(closestCellB.getCentre().getY()*5));

				//Finally, restart cellGen so that it tries again.
				cellGen = (ArrayList<Cell>) cells.clone();
			}
		}
		//Draw corridors.
		//Shuffle the thing and iterate through it, taking the first connection found.
		//Delete the connection coming the other way, and check the direction of the cell it is connected to.
		//Create a cell between the two, such that it extends to a random point (skewed towards the closest point to the other one, somehow) on one Cell, plus one
		//To a random point on the other one.
		//Draw this new Cell in green.
		/*boolean found;
		do {
			found = false;
			Collections.shuffle(cells, random);
			search:
			for (Cell cell : cells) {
				if (cell.getConnectionCount()>0) {
					found = true;
					Cell connection = cell.getConnections().get(0);
					System.out.println(cell.removeConnection(connection));
					System.out.println(connection.removeConnection(cell));
					//Now check which direction the connection is in
					int relX = connection.getX()-cell.getX();
					int relY = connection.getY()-cell.getY();
					if (Math.abs(relY)>Math.abs(relX)) {
						//Check if it's north or south
						int minCoords =	Math.min(cell.getX(), connection.getX());
						int maxCoords = Math.max(cell.getX()+cell.getW(), connection.getX()+connection.getW());
						Cell corridor = null;
						if (relY>0) {
							//North - create a new Cell above cell.
							int gap = relY-cell.getH();
							if (gap<0) {
								corridor = new Cell(new Coord2D(minCoords, cell.getCentre().getY()), Math.abs(relX), connection.getY()-cell.getCentre().getY());
							} else {
								corridor = new Cell(new Coord2D(minCoords, cell.getY()+cell.getH()), maxCoords-minCoords, gap);
							}
							
						} else {
							int gap = -(connection.getH()+relY);
							corridor = new Cell(new Coord2D(minCoords, connection.getY()+connection.getH()), maxCoords-minCoords, gap);
						}
						corridor.addConnection(cell);
						corridor.addConnection(connection);
						System.out.println("Creating corridor at "+corridor.toStringShort());
						window.repaintCell(corridor, Color.GREEN);
						//Check if this overlaps anything at all.
						for (Cell cellC : cells) {
							if (corridor.strictlyOverlaps(cellC)) {
								System.out.println("Corridor "+corridor.toStringShort()+" overlaps cell "+cellC.toStringShort()+", "+corridor.overlaps(cellC));
								if (corridor.isRightOf(cellC)) {
									//If the right side of the cell is to the left of the right of the parent cell, it's possible to truncate the left of the corridor.
									if (cellC.getX()+cellC.getW()+2<cell.getX()+cell.getW()) {
										//Then truncate the left of the corridor.
									} else {
										//We have a problem. No idea what to do here? Maybe add an Entrance to the obstructing Room, then add another Corridor linking that one and the 2nd Room.
									}
								} else {
									//Otherwise it is to the left. So, do the opposite!
								}
							}
						}
						for (Cell cellC : corridors) {
							if (corridor.strictlyOverlaps(cellC)) {
								System.out.println("Corridor "+corridor.toStringShort()+" overlaps corridor "+cellC.toStringShort()+", "+corridor.overlaps(cellC));
							}
						}
						//And draw the cell.
						corridors.add(corridor);
						break search;
					}
					try {
						Thread.sleep(250);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		} while (found);*/
		
		
		//Another method of corridor drawing - use the A* algorithm.
		//Start at a random point on the 1st cell, which is skewed towards the 2nd. For example, if the 2nd is 9 above and 2 to the left, it is far more likely to pick ones on the top, and a bit more to pick left ones.
		//Do the same with the other one.
		//Then, perform A* until you hit the other point.
		//First, expand the view to see what's going on.

		System.out.println("Beginning A* search.");
		//closed_set should consist of only the cells that are diagonally in between the randomly-generated start and end points.
		for (Connection connection : connections) {
			//Create a random start and end point for the A* algorithm, and run it.
			Cell A = connection.getCellA();
			Cell B = connection.getCellB();
			//Check what side cell B is on, then generate a random number that is most likely to be on the correct side.
			//Only possible to generate the entrance on a given side, if that side is facing at least part of the other cell.
			
			//The probability of this being used is proportional to the "facingness" of the cells. So, work out the angle between them.

			/*Coord2D A1 = A.getCorner();
			Coord2D A2 = A.getLowerLeftCorner();
			Coord2D A3 = A.getLowerRightCorner();
			Coord2D A4 = A.getUpperRightCorner();
			
			Coord2D B1 = B.getCorner();
			Coord2D B2 = B.getLowerLeftCorner();
			Coord2D B3 = B.getLowerRightCorner();
			Coord2D B4 = B.getUpperRightCorner();
			for (int i=1; i<A.getW()-1; i++) {
				//Get the angle the top and bottom cells make with the 4 corners of the other cell.
				//Need to work out the angle that all 4 sides make with the other cell.
				//For each grid cell on the 4 walls of the cell, work out how much of the other cell they can see.
				//Find the angles between the 4 corners. Clamp them to values depending on the wall. Then take the difference between the minimum and maximum. That's the probability this one is going to be used.
				
			}*/
			
			//OH MY GOD FUCK ALL THAT
			//Just work out the angle, and which points intersect that line in each cell. Set those as the start/end points.
			double angle = (A.angleWith(B));
			
			double m = Math.tan(angle);
			
			int h = A.getH();
			int w = A.getW();
			
			Coord2D start = null;
			
			//Need 4 angles, that represent the angle made by the 4 corners.
			//Angles that are between these 4 indicate which side is intersected first.
			double ang1 = Math.atan2(h, w);
			double ang2 = Math.atan2(h, -w);
			double ang3 = ang1+Math.PI;
			double ang4 = ang2+Math.PI;
			
			h /= 2;
			w /= 2;
			//Now, get which side it intersects. We need to check for each quadrant.
			if (angle >= ang1 && angle < ang2) {
				//In this case, it will intersect the bottom side. We know y in this case, it's h. I know it seems upside-down, but it works.
				//If the height is even, it needs to be shifted one up.
				start = new Coord2D(h/m, h);
			} else if (angle >= ang2 && angle < ang3) {
				//Intersects left line. x = -w.
				start = new Coord2D(-w, -m*w);
			} else if (angle >= ang3 && angle < ang4) {
				//Intersects bottom line. y = -h.
				start = new Coord2D(-h/m, -h);
			} else {
				//Intersects right line. x = w.
				start = new Coord2D(w, m*w);
			}
			
			//If it's on a corner, just shove it 1 space up/down.
			if (A.isCorner(Coord2D.sum(start, Coord2D.difference(A.getCentre(), A.getCorner())))) {
				start.moveY((int) -Math.signum(start.getY()));
			}
			
			//Now draw the point.
			window.repaintPoint(Coord2D.sum(A.getCentre(), start), new Color(85, 85, 220));
			
			//Now do the same for the end.
			angle = B.angleWith(A);
			m = Math.tan(angle);
			h = B.getH();
			w = B.getW();
			
			Coord2D end = null;
			
			//Need 4 angles, that represent the angle made by the 4 corners.
			//Angles that are between these 4 indicate which side is intersected first.
			ang1 = Math.atan2(h, w);
			ang2 = Math.atan2(h, -w);
			ang3 = ang1+Math.PI;
			ang4 = ang2+Math.PI;
			
			h /= 2;
			w /= 2;
			//Now, get which side it intersects. We need to check for each quadrant.
			if (angle >= ang1 && angle < ang2) {
				//Intersects bottom line. y = h.
				end = new Coord2D(h/m, h);
			} else if (angle >= ang2 && angle < ang3) {
				//Intersects left line. x = -w.
				end = new Coord2D(-w, -m*w);
			} else if (angle >= ang3 && angle < ang4) {
				//Intersects top line. y = -h.
				end = new Coord2D(-h/m, -h);
			} else {
				//Intersects right line. x = w.
				end = new Coord2D(w, m*w);
			}

			System.out.println(Coord2D.difference(B.getCentre(), B.getCorner()));
			System.out.println(Coord2D.sum(end, Coord2D.difference(B.getCentre(), B.getCorner())));
			if (B.isCorner(Coord2D.sum(end, Coord2D.difference(B.getCentre(), B.getCorner())))) {
				end.moveY((int) -Math.signum(end.getY()));
			}
			
			window.repaintPoint(Coord2D.sum(B.getCentre(), end), new Color(85, 85, 220));
			
			System.out.println();

			ArrayList<Coord2D> path = A_Star(Coord2D.sum(A.getCentre(), start), Coord2D.sum(B.getCentre(), end));
			
			for (Coord2D coord : path) {
				System.out.println(coord);
				window.repaintPoint(coord, new Color(25, 180, 55));
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
			r = (random.nextGaussian()*VARIANCE)+SKEW;
		}
		int s = (int) Math.round(r);
		return s%2==0 ? s+1 : s;
	}
	
	private static boolean triangulate() {
		Collections.shuffle(cells, random);
		for (Cell cell : cells) {
			if (cell.getConnectionCount() < 2) {
				//Create a new Connection to the closest cell that doesn't already have one.
				double closestCellDist = 1000;
				Cell closestCell = null;
				cells.remove(cell);
				for (Cell otherCell : cells) {
					if (cell.getDistanceTo(otherCell)<closestCellDist && !cell.getConnections().contains(otherCell)) {
						closestCell = otherCell;
						closestCellDist = cell.getDistanceTo(otherCell);
					}
				}
				cells.add(cell);
				window.getGraphics().drawLine(CENTREX+(cell.getCentre().getX()*SIZE_MULT), CENTREY+(cell.getCentre().getY()*SIZE_MULT),
						CENTREX+(closestCell.getCentre().getX()*SIZE_MULT), CENTREY+(closestCell.getCentre().getY()*SIZE_MULT));
				
				cell.addConnection(closestCell);
				closestCell.addConnection(cell);
				connections.add(new Connection(cell, closestCell));
				return false;
			}
		}
		return true;
	}
	
	private static ArrayList<Coord2D> A_Star(Coord2D start, Coord2D end) {
		//Naive for now, ignore other cells.
		/* A* Algorithm pseudocode
		 * function A*(start,goal)
		 * 		closedset := the empty set    // The set of nodes already evaluated.
		 * 		openset := {start}    // The set of tentative nodes to be evaluated, initially containing the start node
		 * 		came_from := the empty map    // The map of navigated nodes.
		 * 		
		 * 		g_score[start] := 0    // Cost from start along best known path.
		 * 		// Estimated total cost from start to goal through y.
		 * 		f_score[start] := g_score[start] + heuristic_cost_estimate(start, goal)
		 * 		while openset is not empty
		 * 			current := the node in openset having the lowest f_score[] value
		 * 			if current = goal
		 * 				return reconstruct_path(came_from, goal)
		 * 			remove current from openset
		 * 			add current to closedset
		 * 			for each neighbor in neighbor_nodes(current)
		 * 				if neighbor in closedset
		 * 					continue
		 * 				tentative_g_score := g_score[current] + dist_between(current,neighbor)
		 * 				
		 * 				if neighbor not in openset or tentative_g_score < g_score[neighbor]
		 * 					came_from[neighbor] := current
		 * 					g_score[neighbor] := tentative_g_score
		 * 					f_score[neighbor] := g_score[neighbor] + heuristic_cost_estimate(neighbor, goal)
		 * 					if neighbor not in openset
		 * 						add neighbor to openset
		 * 
		 *		return failure
		 * 
		 * function reconstruct_path(came_from,current)
		 * 		total_path := [current]
		 * 		while current in came_from:
		 * 			current := came_from[current]
		 * 			total_path.append(current)
		 * 		return total_path
		 */
		
		ArrayList<Coord2D> closedSet = new ArrayList<Coord2D>();
		ArrayList<Coord2D> openSet = new ArrayList<Coord2D>();
		openSet.add(start);
		
		//The first one is the new node - it "came from" the second.
		HashMap<Coord2D, Coord2D> cameFrom = new HashMap<Coord2D, Coord2D>();
		
		HashMap<Coord2D, Double> gScore = new HashMap<Coord2D, Double>();
		HashMap<Coord2D, Double> fScore = new HashMap<Coord2D, Double>();
		gScore.put(start, 0.0);
		
		double gSc = gScore.get(start);
		
		
		int estimate = costEstimate(start, end);
		
		fScore.put(start, gSc+estimate);
		Coord2D current;
		cameFrom.put(start, new Coord2D(0, 0));
		while (!openSet.isEmpty()) {
			current = getLowestOf(openSet, fScore);
			if (current==end) {
				return reconstructPath(cameFrom, end);
			}
			openSet.remove(current);
			closedSet.add(current);
			window.repaintPoint(current, new Color(155, 25, 155));
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			for (Coord2D neighbour : current.getOrthogonalNeighbours()) {
				if (!containedInCells(neighbour)) {
					if (!closedSet.contains(neighbour)) {
						window.repaintPoint(neighbour, new Color(180, 120, 40));
						//neighbour "came from" current
						cameFrom.put(neighbour, current);
					
						//If the path has not made a turn (i.e. if current-cameFrom(current) = neighbour-current), score is 1, otherwise 1.25.
						double tentativeG = gScore.get(current) +
								(Coord2D.difference(current, cameFrom.get(current)).equals(Coord2D.difference(neighbour, current)) ? 1 : 1.25);

						try {
							openSet.contains(neighbour);
						} catch (NullPointerException e) {
							System.out.println("OpenSet crashed!");
							e.printStackTrace();
						}
						try {
							gScore.get(neighbour);
						} catch (NullPointerException e) {
							System.out.println("gScore crashed!");
							e.printStackTrace();
						}
						if (!openSet.contains(neighbour) || tentativeG < gScore.get(neighbour)) {
							cameFrom.put(neighbour, current);
							gScore.put(neighbour, tentativeG);
							fScore.put(neighbour, gScore.get(neighbour) + costEstimate(neighbour, end));
							if (!openSet.contains(neighbour)) {
								openSet.add(neighbour);
							}
						}
						try {
							Thread.sleep(250);
						} catch (InterruptedException e) {
							e.printStackTrace();
						} finally {
							window.repaintPoint(neighbour, new Color(140, 80, 0));
						}
					} else {
						window.repaintPoint(neighbour, new Color(100, 40, 0));
					}
				} else {
					window.repaintPoint(neighbour, new Color(200, 10, 10));
				}
			}
			window.repaintPoint(current, new Color(140, 80, 0));
		}
		//If it gets here, it has failed.
		System.out.println("Failure.");
		System.exit(2);
		return null;
	}
	
	private static ArrayList<Coord2D> reconstructPath(HashMap<Coord2D, Coord2D> cameFrom, Coord2D current) {
		ArrayList<Coord2D> totalPath = new ArrayList<Coord2D>();
		while (cameFrom.containsKey(current)) {
			current = cameFrom.get(current);
			totalPath.add(current);
		}
		return totalPath;
	}
	
	private static int costEstimate(Coord2D node, Coord2D goal) {
		return Math.abs(goal.getX()-node.getX())+Math.abs(goal.getY()-node.getY());
	}
	
	private static Coord2D getLowestOf(ArrayList<Coord2D> set, HashMap<Coord2D, Double> scores) {
		Coord2D selected = set.get(0);
		double minScore = scores.get(selected);
		for (Coord2D coord : scores.keySet()) {
			if (set.contains(coord) && scores.get(coord)<minScore) {
				minScore = scores.get(coord);
				selected = coord;
			}
		}
		return selected;
	}
	
	private static ArrayList<Cell> copyCells(ArrayList<Cell> cells) {
		ArrayList<Cell> newCells = new ArrayList<Cell>();
		for (Cell cell : cells) {
			newCells.add(new Cell(new Coord2D(cell.getCorner().getX(), cell.getCorner().getY()), cell.getW(), cell.getH()));
		}
		return newCells;
	}
	
	private static boolean containedInCells(Coord2D coord) {
		for (Cell cell : cells) {
			if (cell.contains(coord)) {
				return true;
			}
		}
		return false;
	}
}
