package com.komiamiko.fcorbit;

import com.komiamiko.fcorbit.document.FCObj;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

public class CommandRotate implements ActiveCommand {

    public static final int PIVOT_AVERAGE = 0;
    public static final int PIVOT_FIRST = 1;
    public static final int PIVOT_WORLD_ORIGIN = 2;
    public static final int PIVOT_INDIVIDUAL = 3;

    public final GraphicEditorPane view;

    public boolean done = false;
    public FCObj[] backupDoc;
    public int initialx;
    public int initialy;

    /**
     * Affects how rotation will be calculated when multiple objects are involved.
     * 0 = average of objects' positions (default)
     * 1 = first object's position
     * 2 = world origin
     * 3 = individual origins
     */
    public int pivotMode;

    public CommandRotate(GraphicEditorPane view){
        this.view = view;
        initialx = view.lastMousex;
        initialy = view.lastMousey;
        backupDoc = new FCObj[view.objSel.cardinality()];
        for(int i = view.objSel.nextSetBit(0), j = 0; i >= 0; i = view.objSel.nextSetBit(i+1), ++j) {
            FCObj obj = (FCObj)view.objDoc.get(i);
            backupDoc[j] = new FCObj(obj);
        }
    }

    @Override
    public void cancel() {
        if(!done)restoreBackupDoc();
        view.repaint();
    }

    private void restoreBackupDoc() {
        for(int i = view.objSel.nextSetBit(0), j = 0; i >= 0; i = view.objSel.nextSetBit(i+1), ++j) {
            FCObj copy = backupDoc[j];
            FCObj obj = (FCObj)view.objDoc.get(i);
            obj.copyFrom(copy);
        }
    }

    @Override
    public void render(Graphics2D g) {
        // compute values
        double[] stats = getRotationStatistics();
        final double screenPivotX = stats[2];
        final double screenPivotY = stats[3];
        // setup render
        g.setColor(GraphicEditorPane.AXISXY);
        g.setStroke(new BasicStroke(1));
        // draw lines
        double radius = Math.max(100, Math.hypot(initialx - screenPivotX, initialy - screenPivotY));
        g.draw(new Ellipse2D.Double(screenPivotX - radius, screenPivotY - radius, radius * 2, radius * 2));
        double px;
        double py;
        double pmult;
        px = initialx;
        py = initialy;
        px -= screenPivotX;
        py -= screenPivotY;
        pmult = radius / Math.max(1, Math.hypot(px, py));
        px = px * pmult + screenPivotX;
        py = py * pmult + screenPivotY;
        g.draw(new Line2D.Double(screenPivotX, screenPivotY, px, py));
        px = view.lastMousex;
        py = view.lastMousey;
        px -= screenPivotX;
        py -= screenPivotY;
        pmult = radius / Math.max(1, Math.hypot(px, py));
        px = px * pmult + screenPivotX;
        py = py * pmult + screenPivotY;
        g.draw(new Line2D.Double(screenPivotX, screenPivotY, px, py));
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {
        switch(keyEvent.getKeyCode()){
            case KeyEvent.VK_ESCAPE:{
                view.cancelCommand();
                break;
            }
            case KeyEvent.VK_PERIOD:{
                pivotMode++;
                pivotMode %= 4;
                updateMove();
                view.repaint();
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
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
    public void mouseEntered(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {
        // Do movement
        updateMove();
        view.repaint();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
    }

    private void updateMove() {
        restoreBackupDoc();
        double[] stats = getRotationStatistics();
        final double allpivotx = stats[0];
        final double allpivoty = stats[1];
        final double angleDiffRadians = stats[6];
        final double angleDiffDegrees = stats[7];
        double c = Math.cos(angleDiffRadians);
        double s = Math.sin(angleDiffRadians);
        for(int i = view.objSel.nextSetBit(0), j = 0; i >= 0; i = view.objSel.nextSetBit(i+1), ++j) {
            FCObj obj = (FCObj)view.objDoc.get(i);
            obj.r += angleDiffDegrees;
            if(pivotMode != PIVOT_INDIVIDUAL) {
                double dx = obj.x - allpivotx;
                double dy = obj.y - allpivoty;
                obj.x = allpivotx + dx * c - dy * s;
                obj.y = allpivoty + dx * s + dy * c;
            }
        }
    }

    /*
     * world x, world y, screen x, screen y, initial angle, final angle, angle diff radians, angle diff degrees
     */
    private double[] getRotationStatistics() {
        // calculate pivot in world
        int objcount = 0;
        double allpivotx = 0;
        double allpivoty = 0;
        for(int i = view.objSel.nextSetBit(0), j = 0; i >= 0; i = view.objSel.nextSetBit(i+1), ++j) {
            if(pivotMode == PIVOT_WORLD_ORIGIN) {
                break;
            }
            FCObj obj = (FCObj)view.objDoc.get(i);
            objcount++;
            allpivotx += obj.x;
            allpivoty += obj.y;
            if(pivotMode == PIVOT_FIRST) {
                break;
            }
        }
        allpivotx /= Math.max(1, objcount);
        allpivoty /= Math.max(1, objcount);
        // calculate pivot on screen
        double[] viewPivot = view.coordinateWorldToScreen(new double[]{allpivotx, allpivoty});
        final double viewPivotx = viewPivot[0];
        final double viewPivoty = viewPivot[1];
        // calculate angle
        double initialAngle = Math.atan2(initialy - viewPivoty, initialx - viewPivotx);
        double finalAngle = Math.atan2(view.lastMousey - viewPivoty, view.lastMousex - viewPivotx);
        double angleDiffRadians = finalAngle - initialAngle;
        double angleDiffDegrees = Math.toDegrees(angleDiffRadians);
        if(angleDiffDegrees > 180) {
            angleDiffDegrees -= 360;
        }
        if(angleDiffDegrees < -180) {
            angleDiffDegrees += 360;
        }
        return new double[]{allpivotx, allpivoty, viewPivotx, viewPivoty, initialAngle, finalAngle, angleDiffRadians, angleDiffDegrees};
    }
}
