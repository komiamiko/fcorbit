package com.komiamiko.fcorbit;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;

import com.komiamiko.fcorbit.document.FCObj;

/**
 * 
 * 
 * @author EPICI
 * @version 1.0
 */
public class CommandTranslate implements ActiveCommand{
	
	public final GraphicEditorPane view;
	
	public boolean done = false;
	public FCObj[] backupDoc;
	public int initialx;
	public int initialy;
	
	/**
	 * Modify the translation direction
	 * <br>
	 * 0 is none, 1 is x local, 2 is x global,
	 * -1 is y local, -2 is y global
	 */
	public int direction = 0;
	
	public CommandTranslate(GraphicEditorPane view){
		this.view = view;
		initialx = view.lastMousex;
		initialy = view.lastMousey;
		backupDoc = new FCObj[view.objSel.cardinality()];
		for(int i = view.objSel.nextSetBit(0), j = 0; i >= 0; i = view.objSel.nextSetBit(i+1), ++j) {
			FCObj obj = (FCObj)view.objDoc.get(i);
			backupDoc[j] = new FCObj(obj);
		}
	}
	
	public void restoreBackupDoc(){
		for(int i = view.objSel.nextSetBit(0), j = 0; i >= 0; i = view.objSel.nextSetBit(i+1), ++j) {
			FCObj copy = backupDoc[j];
			FCObj obj = (FCObj)view.objDoc.get(i);
			obj.copyFrom(copy);
		}
	}
	
	public double[] getTranslation(int mx,int my){
		final int direction = this.direction;
		int dx = mx-initialx;
		int dy = my-initialy;
		final double invScale = view.getInvScale();
		double wdx = dx*invScale;
		double wdy = dy*invScale;
		switch(direction){
		case 1:{
			final double r = view.getPivot()[2],
					cr = Math.cos(r),
					sr = Math.sin(r);
			double wdl = wdx;
			wdx = wdl*cr;
			wdy = wdl*sr;
			break;
		}
		case 2:{
			wdy=0;
			break;
		}
		case -1:{
			final double r = view.getPivot()[2],
					cr = Math.cos(r),
					sr = Math.sin(r);
			double wdl = wdy;
			wdy = wdl*cr;
			wdx = -wdl*sr;
			break;
		}
		case -2:{
			wdx=0;
			break;
		}
		}
		return new double[]{wdx,wdy};
	}
	
	public void updateMove(int mx,int my){
		restoreBackupDoc();
		double[] wdxy = getTranslation(mx,my);
		double wdx = wdxy[0];
		double wdy = wdxy[1];
		for(int i = view.objSel.nextSetBit(0), j = 0; i >= 0; i = view.objSel.nextSetBit(i+1), ++j) {
			FCObj obj = (FCObj)view.objDoc.get(i);
			obj.x += wdx;
			obj.y += wdy;
		}
	}

	@Override
	public void cancel() {
		if(!done)restoreBackupDoc();
		view.repaint();
	}

	@Override
	public void render(Graphics2D g) {
		final double RAYLENGTH = 10000;
		final double scale = view.getScale();
		int width = view.getWidth(), height = view.getHeight();
		double cx = width*0.5, cy = height*0.5;
		AffineTransform ot = g.getTransform();
		g.translate(cx, cy);
		g.scale(scale, scale);
		g.translate(-view.anchorx, -view.anchory);
		double[] pivot = view.getPivot();
		double ox = pivot[0];
		double oy = pivot[1];
		if(direction==0){
			double[] wdxy = getTranslation(view.lastMousex,view.lastMousey);
			double wdx = wdxy[0];
			double wdy = wdxy[1];
			g.setColor(GraphicEditorPane.AXISXY);
			g.drawLine((int)(ox), (int)(oy), (int)(ox-wdx), (int)(oy-wdy));
		}else{
			double wdx,wdy;
			if(direction>0){
				g.setColor(GraphicEditorPane.AXISX);
				if(direction>1){
					wdx=1;
					wdy=0;
				}else{
					final double r = pivot[2];
					wdx = Math.cos(r);
					wdy = Math.sin(r);
				}
			}else{
				g.setColor(GraphicEditorPane.AXISY);
				if(direction<-1){
					wdx=0;
					wdy=1;
				}else{
					final double r = pivot[2];
					wdx = -Math.sin(r);
					wdy = Math.cos(r);
				}
			}
			wdx*=RAYLENGTH;
			wdy*=RAYLENGTH;
			g.drawLine((int)(ox-wdx), (int)(oy-wdy), (int)(ox+wdx), (int)(oy+wdy));
		}
		g.setTransform(ot);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		switch(view.mouseDown){
		case 1:{
			// Left click to confirm
			done=true;
			Main.updateTextFromObj();
			view.cancelCommand();
			break;
		}
		case 3:{
			// Right click to cancel
			view.cancelCommand();
			break;
		}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mouseDragged(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// Do movement
		updateMove(e.getX(),e.getY());
		view.repaint();
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
		switch(e.getKeyCode()){
		case KeyEvent.VK_ESCAPE:{
			view.cancelCommand();
			break;
		}
		case KeyEvent.VK_X:{
			direction = direction>0?direction-1:2;
			updateMove(view.lastMousex,view.lastMousey);
			view.repaint();
			break;
		}
		case KeyEvent.VK_Y:{
			direction = direction<0?direction+1:-2;
			updateMove(view.lastMousex,view.lastMousey);
			view.repaint();
			break;
		}
		}
	}
	
}
