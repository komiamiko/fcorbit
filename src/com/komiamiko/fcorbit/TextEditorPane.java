package com.komiamiko.fcorbit;

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
	
	@Override
	public void setText(String newText) {
		// short circuit on null or empty
		final int newLength;
		if (newText == null || (newLength = newText.length()) == 0) {
			super.setText(newText);
			return;
		}
		String oldText = this.getText();
		final int oldLength = oldText.length();
		// compute longest shared prefix
		int prefix = 0;
		int lim = Math.min(oldLength, newLength);
		for(;prefix < lim && oldText.charAt(prefix)
				== newText.charAt(prefix);++prefix);
		// compute longest shared suffix
		lim -= prefix;
		int suffix = 0;
		for(;suffix < lim && oldText.charAt(oldLength - 1 - suffix)
				== newText.charAt(newLength - 1 - suffix);++suffix);
		// replace the text inbetween
		final int oldStart = prefix, oldStop = oldLength - suffix,
				newStart = prefix, newStop = newLength - suffix;
		String newSlice = newText.substring(newStart, newStop);
		this.replaceRange(newSlice, oldStart, oldStop);
	}

}
