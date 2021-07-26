import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class ClickSelect extends JPanel {
	private static final long serialVersionUID = 1401120576625457816L;
	// The previous rectangle coordinates
	private static int recX, recY, recX2, recY2;
	// To decide whether the chosen coordinates are OK to progress
	private static boolean coordsOK = true;
	
	@Override
	protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Use a red coloured line to draw the rectangle
        g.setColor(Color.RED);
        // Draw the rectangle with the starting x and y coordinate as the minimum of the 2 points
		int x = Math.min(recX,recX2);
        int y = Math.min(recY,recY2);
        // The width and height is the absolute value (non negative) of the difference of the 2 points' X or Y coordinate
        int w = Math.abs(recX-recX2);
        int h = Math.abs(recY-recY2);
        // Draw the rectangle using the adjusted points and width/height
        g.drawRect(x, y, w, h);
	}
	
	// This would be called initially to show the rectangle that was made previously
	private static void updateCoords(String value){
		// If there was no initial value, the rectangle points all start at 0
		if (value == null){
			recX = 0;
			recY = 0;
			recX2 = 0;
			recY2 = 0;
		}
		// Check that the point is also not empty
		else if (value.length() > 0){
			// Make sure that there are 2 coordinates, otherwise it is a single point making it not eligible for a rectangle
			if (value.contains("-")){
				// The first point is before the - and the second is after it
				String pt1 = value.substring(0, value.indexOf('-'));
				String pt2 = value.substring(value.indexOf('-')+1);
				// The X coordinate of the first point is what's before the comma of the first point
				recX = Integer.valueOf(pt1.substring(0, pt1.indexOf(',')));
				// The Y coordinate of the first point is what's after the comma of the first point
				recY = Integer.valueOf(pt1.substring(pt1.indexOf(',')+1));
				// The X coordinate of the second point is what's before the comma of the second point
				recX2 = Integer.valueOf(pt2.substring(0, pt2.indexOf(',')));
				// The Y coordinate of the second point is what's after the comma of the second point
				recY2 = Integer.valueOf(pt2.substring(pt2.indexOf(',')+1));
				
			}
			// If there is only 1 point, the rectangle points all start at 0
			else {
				recX = 0;
				recY = 0;
				recX2 = 0;
				recY2 = 0;
			}
		}
		// If the value is an empty string, it is as if it's a null value and the rectangle points all start at 0
		else {
			recX = 0;
			recY = 0;
			recX2 = 0;
			recY2 = 0;
		}
	}
	
	/*
	 * Create an empty transparent panel that detects where the user chooses a point or a point range (rectangle)
	 * and sends the data back to the Main class when done. The frame is disposed when done.
	 * @param clickFrame	The frame that was created for this panel
	 * @param value			The value that was set before calling this method for the rectangle points
	 * @param XYRow			The table row that the points value came from
	 * @param commandTable	Whether or not this is a call from the main command table (true if it is)
	 */
	public ClickSelect(JFrame clickFrame, String value, int XYRow, boolean commandTable){
		// Set the panel to be transparent so the user can see where they are clicking behind the frame
		setOpaque(false);
		// Update the initial coordinates so the rectangle previously made and stored as value shows up first
		updateCoords(value);
		
		// Cancel operation and dispose of the frame after maximising the window, if Esc key is pressed
		clickFrame.addKeyListener(new KeyAdapter() {
		    public void keyReleased(KeyEvent e) {
		    	if (e.getKeyCode() == KeyEvent.VK_ESCAPE){
		    		Main.frame.setState(Frame.NORMAL);
		    		clickFrame.dispose();
		        }
		    }
		});
		
		// Set the array for storing the points the user chooses immediately as the mouse is pressed/released
		int[] mousePos = new int[4];
		addMouseListener(new MouseListener() {
		    @Override
		    public void mousePressed(MouseEvent event) {
		    	// When the mouse is pressed, store the coordinates in the global variables
		    	// and the appropriate positions in the mousePos array.
				mousePos[0] = event.getX();
				mousePos[1] = event.getY();
				recX = event.getX();
				recY = event.getY();
				// If the mouse is dragged after it had been pressed, a second point can be created
				addMouseMotionListener(new MouseMotionListener(){
					@Override
					public void mouseDragged(MouseEvent evmotion) {
						// Assign the coordinate values of the second point and repaint the panel to show the new rectangle
						recX2 = evmotion.getX();
						recY2 = evmotion.getY();
						repaint();
					}
					public void mouseMoved(MouseEvent evmotion) {/*Unimplemented*/}
				});
			}
		    @Override
			public void mouseReleased(MouseEvent event) {
		    	// After mouse is released, assign the coordinates of the release point in the mousePos array
		    	mousePos[2] = event.getX();
		    	mousePos[3] = event.getY();
		    	// If it's the same point that was chosen when mouse was pressed and it's not for the main command table
		    	// (i.e. image table) then the coordinates chosen are not not OK as it should be a rectangle and not a point
		    	// for a full image to be constructed
		    	if (mousePos[2] == mousePos[0] && mousePos[3] == mousePos[1] && !commandTable){
		    		coordsOK = false;
		    	}
		    	// Otherwise the chosen coordinates are OK
		    	else {
		    		coordsOK = true;
		    	}
		    	
		    	if (coordsOK){
		    		// Dispose of the clickFrame
					clickFrame.dispose();
					// This is for constructing the String that would be returned as the position coordinates chosen
					String pos = "";
					// If the points chosen are different, then there are definitely 2 different points
					if ((mousePos[0] != mousePos[2]) || (mousePos[1] != mousePos[3])){
						// Separate the X and Y coordinates with a comma and the 2 points with a dash
						pos = mousePos[0] + "," + mousePos[1] + "-" + mousePos[2] + "," + mousePos[3];
					}
					// Otherwise, there is only 1 point chosen and only the first X and Y coordinates are needed 
					else {
						// Separate them with a comma
						pos = mousePos[0] + "," + mousePos[1];
					}
					
					// If this is for the main command table, tell the Main class to update the main table
					// using the pos String that was constructed and the row that was given when this method was called
					if (commandTable){
						Main.updateTableCoords(pos, XYRow);
					}
					// Otherwise, this is for the image condition table, so tell the Main class to update the image table
					// using the pos String that was constructed and the row that was given when this method was called
					else {
						Main.updateImageCoords(pos, XYRow);
					}
		    	}
		    }
			public void mouseClicked(MouseEvent event) {/*Unimplemented*/}
			public void mouseEntered(MouseEvent event) {/*Unimplemented*/}
			public void mouseExited(MouseEvent event) {/*Unimplemented*/}
		});
	}
	
}
