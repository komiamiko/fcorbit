package com.komiamiko.fcorbit;

import com.komiamiko.fcorbit.document.FCObj;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

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
    public int viewPivotx;
    public int viewPivoty;

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
        viewPivotx = view.getWidth() / 2;
        viewPivoty = view.getHeight() / 2;
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
        // TODO
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
        updateMove(mouseEvent.getX(),mouseEvent.getY());
        view.repaint();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
    }

    private void updateMove(int x, int y) {
        restoreBackupDoc();
        double initialAngle = Math.atan2(initialy - viewPivoty, initialx - viewPivotx);
        double finalAngle = Math.atan2(y - viewPivoty, x - viewPivotx);
        double angleDiffRadians = finalAngle - initialAngle;
        double angleDiffDegrees = Math.toDegrees(angleDiffRadians);
        if(angleDiffDegrees > 180) {
            angleDiffDegrees -= 360;
        }
        if(angleDiffDegrees < -180) {
            angleDiffDegrees += 360;
        }
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
}
