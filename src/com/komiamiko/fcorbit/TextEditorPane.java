package core;

import java.util.BitSet;

import javax.swing.JTextArea;
import javax.swing.text.Document;

/**
 * The text editor component
 * 
 * @author EPICI
 * @version 1.0
 */
public class TextEditorPane extends JTextArea implements KeyTracker {
	private static final long serialVersionUID = -6486247017222620294L;
	
	/**
	 * Which keys are held down
	 */
	public final BitSet keys = new BitSet();
	
	@Override
	public BitSet getKeys(){
		return keys;
	}

	public TextEditorPane() {
	}

	public TextEditorPane(String text) {
		super(text);
	}

	public TextEditorPane(Document doc) {
		super(doc);
	}

	public TextEditorPane(int rows, int columns) {
		super(rows, columns);
	}

	public TextEditorPane(String text, int rows, int columns) {
		super(text, rows, columns);
	}

	public TextEditorPane(Document doc, String text, int rows, int columns) {
		super(doc, text, rows, columns);
	}
	
	/**
	 * Call after done initializing fields
	 */
	public void init(){
		
	}
	
	@Override
	public void forget(){
		keys.clear();
	}

}
