package com.komiamiko.fcorbit.document;

import java.util.Objects;

/**
 * Represents a comment line, which is any line that does not
 * represent an FC object.
 * 
 * @author komiamiko
 * @version 1.0
 */
public class CommentLine implements FCDocumentLine {
	
	/**
	 * Line number, as would be returned by {@link #getLineNumber()}
	 */
	protected int line;
	/**
	 * Sub-line number, as would be returned by {@link #getSubLineNumber()}
	 */
	protected int subline;
	/**
	 * The text of this line.
	 */
	public String text;
	
	/**
	 * Usual constructor, with text and line number.
	 * 
	 * @param text text of the line
	 * @param line line number
	 */
	public CommentLine(String text, int line) {
		this.text = text;
		setLineNumber(line, 0);
	}

	@Override
	public int getLineNumber() {
		return line;
	}

	@Override
	public int getSubLineNumber() {
		return subline;
	}

	@Override
	public void setLineNumber(int line, int subline) {
		this.line = line;
		this.subline = subline;
	}

	@Override
	public int hashCode() {
		return Objects.hash(text);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof CommentLine))
			return false;
		CommentLine other = (CommentLine) obj;
		return Objects.equals(text, other.text);
	}
	
	@Override
	public String toString() {
		return text;
	}

}
