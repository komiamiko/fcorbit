package core;

import java.awt.Graphics2D;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;

/**
 * Represents either an active command or the root listener set
 * 
 * @author EPICI
 * @version 1.0
 */
public interface ActiveCommand extends MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {
	
	/**
	 * Cancel the command
	 */
	public void cancel();
	
	/**
	 * Render this command's overlay
	 * 
	 * @param g
	 */
	public void render(Graphics2D g);
	
}
