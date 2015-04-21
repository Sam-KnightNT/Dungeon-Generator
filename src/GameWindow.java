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
	private ArrayList<Cell> cells;
	
	public GameWindow(ArrayList<Cell> cells) {
		setBorder(BorderFactory.createLineBorder(Color.black));
		this.cells = cells;
	}

	public void paintComponent(Graphics g) {
		super.paintComponents(g);
	}
	
	public void repaintCell(Cell cell) {
		Graphics g = getGraphics();
		if (cell.getW()*cell.getH()>20) {
			g.setColor(Color.RED);
		} else {
			g.setColor(Color.BLUE);
		}
        g.fillRect(CENTREX+(cell.getCorner().getX()*5), CENTREY+(cell.getCorner().getY()*5), cell.getW()*5, cell.getH()*5);
        g.setColor(Color.BLACK);
        //g.drawRect(CENTREX+(cell.getCorner().getX()*5), CENTREY+(cell.getCorner().getY()*5), cell.getW()*5, cell.getH()*5); 
	}
	
	public void repaintCell(Cell cell, Color colour) {
		Graphics g = getGraphics();
		g.setColor(colour);
        g.fillRect(CENTREX+(cell.getCorner().getX()*5), CENTREY+(cell.getCorner().getY()*5), cell.getW()*5, cell.getH()*5);
        g.setColor(Color.BLACK);
        //g.drawRect(CENTREX+(cell.getCorner().getX()*5), CENTREY+(cell.getCorner().getY()*5), cell.getW()*5, cell.getH()*5);
        
	}
	public void clearCell(Cell cell) {
    	getGraphics().clearRect(CENTREX+(cell.getCorner().getX()*5), CENTREY+(cell.getCorner().getY()*5), cell.getW()*5+1, cell.getH()*5+1);
	}

	public void drawCellPart(Color colour, Cell cell, int size) {
		Graphics g = getGraphics();
		g.setColor(colour);
		g.fillRect(CENTREX+(cell.getCentre().getX()*5)-size, CENTREY+(cell.getCentre().getY()*5)-size, size*2, size*2);
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
