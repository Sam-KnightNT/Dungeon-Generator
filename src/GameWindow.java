import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import javax.swing.*;


public class GameWindow extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5446208496818554929L;
	
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
        g.fillRect(500+(cell.getCorner().getX()*5), 500+(cell.getCorner().getY()*5), cell.getW()*5, cell.getH()*5);
        g.setColor(Color.BLACK);
        g.drawRect(500+(cell.getCorner().getX()*5), 500+(cell.getCorner().getY()*5), cell.getW()*5, cell.getH()*5); 
	}
	
	public void clearCell(Cell cell) {
    	getGraphics().clearRect(500+(cell.getCorner().getX()*5), 500+(cell.getCorner().getY()*5), cell.getW()*5+1, cell.getH()*5+1);
	}
}
