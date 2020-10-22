package com.komiamiko.fcorbit.document;

/**
 * Dummy type to act as a union between text lines
 * representing FC objects and other text lines.
 * 
 * @author komiamiko
 * @version 1.0
 */
public interface FCDocumentLine {
	
	/**
	 * Get the line number within the document.
	 * <br>
	 * Lines are sorted by lexicographic order on (line, subline).
	 * After any operation is finalized, the line numbers are always
	 * normalized so that line counts up and subline = 0.
	 * In an intermediate step, subline may be useful to instruct
	 * the editor to insert multiple lines at a location.
	 * <br>
	 * Line number is not to be used in equality or hashing.
	 * 
	 * @return the line number
	 */
	public int getLineNumber();
	
	/**
	 * Get the sub-line number.
	 * 
	 * @see FCDocumentLine#getLineNumber()
	 * 
	 * @return the sub-line number
	 */
	public int getSubLineNumber();
	
	/**
	 * Set the line number and sub-line number at once.
	 * 
	 * @param line
	 * @param subline
	 */
	public void setLineNumber(int line, int subline);
	
}
