import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import javax.swing.*;

public class GameWindow extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5446208496818554929L;
	private static final int CENTREX = 500;
	private static final int CENTREY = 375;
	private static final int SIZE_MULT = 5;
	private Graphics g;
	private ArrayList<Cell> cells;
	
	public GameWindow(ArrayList<Cell> cells) {
		setBorder(BorderFactory.createLineBorder(Color.black));
		this.cells = cells;
	}

	public void setGraphics() {
		g = getGraphics();
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponents(g);
	}
	
	public void repaintPoint(Coord2D point, Color colour) {
		g.setColor(colour);
		g.fillRect(CENTREX+(point.getX()*SIZE_MULT), CENTREY+(point.getY()*SIZE_MULT), SIZE_MULT, SIZE_MULT);
	}
	
	public void repaintCell(Cell cell) {
		if (cell.getW()*cell.getH()>20) {
			g.setColor(Color.RED);
		} else {
			g.setColor(Color.BLUE);
		}
        g.fillRect(CENTREX+(cell.getCorner().getX()*SIZE_MULT), CENTREY+(cell.getCorner().getY()*SIZE_MULT), cell.getW()*SIZE_MULT, cell.getH()*SIZE_MULT);
        g.setColor(Color.BLACK);
        //g.drawRect(CENTREX+(cell.getCorner().getX()*5), CENTREY+(cell.getCorner().getY()*5), cell.getW()*5, cell.getH()*5); 
	}
	
	public void repaintCell(Cell cell, Color colour) {
		g.setColor(colour);
        g.fillRect(CENTREX+(cell.getCorner().getX()*SIZE_MULT), CENTREY+(cell.getCorner().getY()*SIZE_MULT), cell.getW()*SIZE_MULT, cell.getH()*SIZE_MULT);
        g.setColor(Color.BLACK);
        //g.drawRect(CENTREX+(cell.getCorner().getX()*5), CENTREY+(cell.getCorner().getY()*5), cell.getW()*5, cell.getH()*5);
        
	}
	public void clearCell(Cell cell) {
    	getGraphics().clearRect(CENTREX+(cell.getCorner().getX()*SIZE_MULT), CENTREY+(cell.getCorner().getY()*SIZE_MULT), cell.getW()*SIZE_MULT+1, cell.getH()*SIZE_MULT+1);
	}

	public void drawCellPart(Color colour, Cell cell, int size) {
		g.setColor(colour);
		g.fillRect(CENTREX+(cell.getCentre().getX()*SIZE_MULT)-size, CENTREY+(cell.getCentre().getY()*SIZE_MULT)-size, size*2, size*2);
	}

	public void transpose(ArrayList<Cell> cells, Coord2D transposeTo) {
		
	}
	public void slowTranspose(ArrayList<Cell> cells, Coord2D transposeTo) {
		while(!transposeTo.isZero()) {
			Coord2D partialTranspose = transposeTo.shiftTo();
			System.out.println(transposeTo+", "+partialTranspose);
			transposeTo.subtract(partialTranspose);
			//Transpose the cells to the appropriate place
			for (Cell cell : cells) {
				clearCell(cell);
				cell.setCorner(cell.getCorner().subtract(partialTranspose));
			}
			
			//Redraw them on-screen
			for (Cell cell : cells) {
				repaintCell(cell);
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
