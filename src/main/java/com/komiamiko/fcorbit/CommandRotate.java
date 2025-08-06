package com.komiamiko.fcorbit;

import com.komiamiko.fcorbit.document.FCObj;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class CommandRotate implements ActiveCommand {

    public final GraphicEditorPane view;

    public boolean done = false;
    public FCObj[] backupDoc;
    public int initialx;
    public int initialy;
    public int viewPivotx;
    public int viewPivoty;

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
        // TODO
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        // TODO
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {
        // TODO
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        // TODO
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        // TODO
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        // TODO
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {
        // TODO
    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {
        // TODO
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        // TODO
    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {
        // TODO
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
        // TODO
    }
}
