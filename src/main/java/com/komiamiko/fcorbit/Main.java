package com.komiamiko.fcorbit;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TimerTask;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.WindowConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import com.komiamiko.fcorbit.document.CommentLine;
import com.komiamiko.fcorbit.document.FCDocumentLine;
import com.komiamiko.fcorbit.document.FCObj;

/**
 * The main class
 * 
 * @author EPICI
 * @version 1.0
 */
public class Main {
	
	/**
	 * Main window
	 */
	public static JFrame frame;
	/**
	 * Pane that holds the two editor views
	 */
	public static JSplitPane splitPane;
	/**
	 * Editor graphic view
	 */
	public static GraphicEditorPane graphicEditor;
	/**
	 * Scroll pane for editor text view
	 */
	public static JScrollPane textEditorScroll;
	/**
	 * Editor text view
	 */
	public static TextEditorPane textEditor;
	/**
	 * Editor text view document
	 */
	public static Document textDoc;
	/**
	 * Editor text view selection
	 */
	public static String textSel;
	/**
	 * Undo manager object which tracks changes and handles undo/redo
	 */
	public static TimedUndoManagerV2 textUndo;
	/**
	 * Editor internal document
	 */
	public static ArrayList<FCDocumentLine> objDoc;
	/**
	 * Editor internal selection
	 */
	public static BitSet objSel;
	/**
	 * Changes every update, used to track idling
	 */
	public static long ticker;
	
	public static final PrintStream console = System.out;

	/**
	 * The main method
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// Make the graphical objects
		frame = new JFrame("Orbit - Editor for Fantastic Contraption");
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		textEditorScroll = new JScrollPane();
		graphicEditor = new GraphicEditorPane();
		textDoc = new PlainDocument();
		textEditor = new TextEditorPane(textDoc,"",150,150);
		textSel = textEditor.getSelectedText();
		objDoc = new ArrayList<>();
		objSel = new BitSet();
		textUndo = new TimedUndoManagerV2();
		// Do layout
		textEditorScroll.setViewportView(textEditor);
		splitPane.add(graphicEditor);
		splitPane.add(textEditorScroll);
		frame.add(splitPane);
		// Set fields and initialize
		graphicEditor.objDoc = objDoc;
		graphicEditor.objSel = objSel;
		graphicEditor.setBackupSel();
		textDoc.addUndoableEditListener(textUndo);
		addListeners();
		graphicEditor.init();
		textEditor.init();
		// Finalize and show
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setResizable(true);
		frame.pack();
		frame.setVisible(true);
	}
	
	public static void addListeners(){
		addKeyTracker(graphicEditor,textEditor);
		addKeyTracker(textEditor,graphicEditor);
		addUndoTracker(textEditor);
		addGraphicForwardListeners();
		textDoc.addDocumentListener(new DocumentListener(){

			@Override
			public void insertUpdate(DocumentEvent e) {
				if(allowUpdateObjFromText())updateObjDocumentFromText();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				if(allowUpdateObjFromText())updateObjDocumentFromText();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				if(allowUpdateObjFromText())updateObjDocumentFromText();
			}
			
		});
		textEditor.addCaretListener(new CaretListener(){
			// Caret is the text cursor, so this does selection listening as well

			@Override
			public void caretUpdate(CaretEvent e) {
				if(allowUpdateObjFromText())updateObjSelectionFromText();
			}
		
		});
		
	}
	
	/**
	 * If true, forces <i>allowUpdateObjFromText()</i> to return true
	 */
	public static boolean overrideUoft;
	/**
	 * Should we allow updating object from text?
	 * 
	 * @return
	 */
	public static boolean allowUpdateObjFromText(){
		return overrideUoft||textEditor.isFocusOwner();
	}
	
	public static void addGraphicForwardListeners(){
		graphicEditor.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent e) {
				// Don't use mouse click
				graphicEditor.command.mouseClicked(e);
			}

			@Override
			public void mousePressed(MouseEvent e) {
				graphicEditor.mouseDown = e.getButton();
				graphicEditor.mouseDragged = false;
				graphicEditor.originMousex = graphicEditor.lastMousex = e.getX();
				graphicEditor.originMousey = graphicEditor.lastMousey = e.getY();
				graphicEditor.uanchorx = graphicEditor.anchorx;
				graphicEditor.uanchory = graphicEditor.anchory;
				graphicEditor.command.mousePressed(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				graphicEditor.command.mouseReleased(e);
				graphicEditor.mouseDown = 0;
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				graphicEditor.command.mouseEntered(e);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				graphicEditor.command.mouseExited(e);
			}
			
		});
		graphicEditor.addMouseMotionListener(new MouseMotionListener(){

			@Override
			public void mouseDragged(MouseEvent e) {
				graphicEditor.mouseDragged = true;
				graphicEditor.command.mouseDragged(e);
				graphicEditor.lastMousex = e.getX();
				graphicEditor.lastMousey = e.getY();
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				graphicEditor.command.mouseMoved(e);
				graphicEditor.lastMousex = e.getX();
				graphicEditor.lastMousey = e.getY();
			}
			
		});
		graphicEditor.addMouseWheelListener(new MouseWheelListener(){

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				graphicEditor.command.mouseWheelMoved(e);
			}
			
		});
		graphicEditor.addKeyListener(new KeyListener(){

			@Override
			public void keyTyped(KeyEvent e) {
				graphicEditor.command.keyTyped(e);
			}

			@Override
			public void keyPressed(KeyEvent e) {
				graphicEditor.command.keyPressed(e);
			}

			@Override
			public void keyReleased(KeyEvent e) {
				graphicEditor.command.keyReleased(e);
			}
			
		});
	}
	
	public static void parseTextTo(String source,ArrayList<FCDocumentLine> target,String format){
		if(source==null)return;
		int lineCounter = 0;
		for(String line:source.split("\n")){
			try{
				target.add(new FCObj(line,format,lineCounter));
			}catch(Exception e){
				target.add(new CommentLine(line,lineCounter));
			}
			lineCounter++;
		}
	}
	
	public static void updateObjFromText(){
		updateObjDocumentFromText();
		updateObjSelectionFromText();
	}
	
	public static void updateObjDocumentFromText(){
		String text = textEditor.getText();
		objDoc.clear();
		parseTextTo(text,objDoc,"fcml");
		ticker++;
		graphicEditor.repaint();
	}
	
	public static void updateObjSelectionFromText(){
		int tsStart = textEditor.getSelectionStart();
		int tsStop = textEditor.getSelectionEnd();
		objSel.clear();
		// empty case
		if(tsStart < tsStop) {
			String text = textEditor.getText();
			int docLength = text.length();
			// skip over whitespace, including empty lines
			while(tsStart < tsStop && text.charAt(tsStart) <= ' ') {
				tsStart++;
			}
			while(tsStart < tsStop && text.charAt(tsStop - 1) <= ' ') {
				tsStop--;
			}
			// stop early if selection is empty
			if(tsStart < tsStop) {
				// count newlines in range
				int nlLeft = 0, nlMid = 0;
				for(int i = 0; i < tsStart; ++i) {
					if(text.charAt(i) == '\n') {
						nlLeft++;
					}
				}
				for(int i = tsStart; i < tsStop; ++i) {
					if(text.charAt(i) == '\n') {
						nlMid++;
					}
				}
				// determine start/stop of the selection
				int selStart = nlLeft;
				int selStop = nlLeft + nlMid + 1;
				// set selection only for fc object lines
				for(int i = selStart; i < selStop; ++i) {
					if(objDoc.get(i) instanceof FCObj) {
						objSel.set(i);
					}
				}
			}
		}
		ticker++;
		graphicEditor.setBackupSel();
		graphicEditor.repaint();
	}
	
	public static void updateTextFromObj(){
		textUndo.induceGap();
		updateTextDocumentFromObj();
		updateTextSelectionFromObj();
		textUndo.induceGap();
	}
	
	public static void updateTextDocumentFromObj(){
		fixLineNumbers(objDoc);
		StringBuilder sb = new StringBuilder();
		for(FCDocumentLine obj:objDoc){
			sb.append(obj.toString());
			sb.append('\n');
		}
		textEditor.setText(sb.toString());
		ticker++;
		textEditor.repaint();
	}
	
	public static void updateTextSelectionFromObj(){
		graphicEditor.setBackupSel();
		int sn = objSel.cardinality();
		// check not empty
		if(sn>0){
			// check contiguous
			// if they are contiguous, it will look like
			// 0 ... 0 1 ... 1 0 ... 0
			// so the first 0 after the first 1 should be just after the last 1
			final int firstSet = objSel.nextSetBit(0);
			final int lastSet = objSel.previousSetBit(objSel.length());
			final int nextClear = objSel.nextClearBit(firstSet);
			if(nextClear == lastSet + 1) {
				final int selStart = firstSet;
				final int selStop = nextClear;
				// calculate positions in text
				int tsStart = selStart;
				String[] lines = textEditor.getText().split("\n");
				for(int i = 0; i < selStart; ++i) {
					tsStart += lines[i].length();
				}
				int tsStop = tsStart + selStop - selStart - 1;
				for(int i = selStart; i < selStop; ++i) {
					tsStop += lines[i].length();
				}
				// temporarily set start to 0 to prevent bounds issues
				textEditor.setSelectionStart(0);
				// set stop
				textEditor.setSelectionEnd(tsStop);
				// set start
				textEditor.setSelectionStart(tsStart);
			}
		}
		ticker++;
		textEditor.repaint();
	}
	
	/**
	 * Normalize line numbers in-place
	 * 
	 * @see FCDocumentLine#getLineNumber()
	 * 
	 * @param target document as list of lines
	 */
	public static void fixLineNumbers(ArrayList<FCDocumentLine> target) {
		int lineCounter = 0;
		for(FCDocumentLine line:target) {
			line.setLineNumber(lineCounter, 0);
			lineCounter++;
		}
	}
	
	/**
	 * Apply a change to the object document. This can be summarized in 4 steps:
	 * <ol>
	 * <li>Delete all objects in the original selection</li>
	 * <li>Insert all new objects at the end</li>
	 * <li>Sort by line/subline, which fixes the ordering</li>
	 * <li>Normalize the line numbers using {@link #fixLineNumbers(ArrayList)}</li>
	 * </ol>
	 * Note that any selected and unchanged objects need to be explicitly re-included.
	 * 
	 * @param target document to modify
	 * @param selection object selection
	 * @param toAdd new objects to add
	 */
	public static void applyObjDocumentChange(ArrayList<FCDocumentLine> target, BitSet selection, ArrayList<FCDocumentLine> toAdd) {
		// delete original selected objects
		for(int i = selection.length(); (i = selection.previousSetBit(i-1)) >= 0;) {
			target.remove(i);
		}
		// append new objects
		target.addAll(toAdd);
		// sort by line number
		target.sort(FCDocumentLine.COMPARE_LINE_NUMBER);
		// normalize line numbers
		fixLineNumbers(target);
	}
	
	public static void tryUndo(){
		textUndo.tryUndo();
	}
	
	public static void tryRedo(){
		textUndo.tryRedo();
	}
	
	public static void addUndoTracker(JComponent comp){
		if(!(comp instanceof KeyTracker))throw new IllegalArgumentException("Does not implement the KeyTracker interface");
		KeyTracker kt = (KeyTracker) comp;
		comp.addKeyListener(new KeyListener(){

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				BitSet keys = kt.getKeys();
				boolean ctrl = keys.get(KeyEvent.VK_CONTROL);
				boolean shift = keys.get(KeyEvent.VK_SHIFT);
				boolean alt = keys.get(KeyEvent.VK_ALT);
				switch(e.getKeyCode()){
				case KeyEvent.VK_Z:{
					if(ctrl&&!alt){
						if(shift){// Ctrl+Shift+Z -> redo
							tryRedo();
						}else{// Ctrl+Z -> undo
							tryUndo();
						}
					}
					break;
				}
				case KeyEvent.VK_Y:{
					if(ctrl&&!shift&&!alt){// Ctrl+Y -> redo
						tryRedo();
					}
					break;
				}
				}
			}
			
		});
	}
	
	/**
	 * Add key tracker
	 * 
	 * @param comp
	 * @param other what to pair with
	 */
	public static void addKeyTracker(JComponent comp,JComponent other){
		if(!(comp instanceof KeyTracker))throw new IllegalArgumentException("Does not implement the KeyTracker interface");
		if(!(other instanceof KeyTracker))throw new IllegalArgumentException("Does not implement the KeyTracker interface");
		KeyTracker kt = (KeyTracker) comp;
		KeyTracker kto = (KeyTracker) other;
		comp.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// Can't receive key events if not focused
				e.getComponent().requestFocusInWindow();
				// Only forget when switching between editors
				kto.forget();
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}
			
		});
		comp.addKeyListener(new KeyListener(){

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				kt.getKeys().set(e.getKeyCode());
			}

			@Override
			public void keyReleased(KeyEvent e) {
				kt.getKeys().clear(e.getKeyCode());
			}
			
		});
	}
	
	/**
	 * Sleep for some number of milliseconds
	 * 
	 * @param ms how long to sleep for
	 */
	public static void sleep(long ms){
		try{
			Thread.sleep(ms);
		}catch(InterruptedException e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Schedule something to happen after a delay
	 * 
	 * @param ms how long to wait
	 * @param r what to do after the delay
	 */
	public static void runAfterDelay(long ms,Runnable r){
		new java.util.Timer().schedule(new TimerTask(){
			@Override
			public void run(){
				r.run();
			}
		}, ms);
	}

}
