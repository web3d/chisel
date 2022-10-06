/*
 * @(#)TextLineEnumerator.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.parse;

import java.util.Vector;
import java.util.Enumeration;

/**
 *  Provides an Enumeration for text lines.
 *
 *  Not very useful, should be removed.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.0, 28 Oct 1997
 *
 *  @since           1.0
 */
public class TextLineEnumerator implements Enumeration {

	Vector textLines;
	int currentOffset;

    /**
     *  Class constructor
     */
	public TextLineEnumerator( TextLineParser p ) {
		textLines = p.getLines();
		currentOffset = 0;
	}

    /**
     *  Are there more lines in the enumeration
     *
     *  @return  true if there are more lines, otherwise false
     */
	public boolean hasMoreElements() {
		if ( currentOffset < textLines.size() ) {
			return( true );
		} else {
			return( false );
		}
	}

    /**
     *  Get the next line in the enumeration.
     *
     *  @return  String form of the next line, null if no more.
     */
	public Object nextElement() {
		Object result = textLines.elementAt( currentOffset );
		currentOffset++;
		return( result );
	}

    /**
     *  How many lines are in the enumeration
     *
     *  @return number of lines in the enumeration
     */
	public int size() {
		return( textLines.size() );
	}

    /**
     *  Get a specific line, given the line number.
     *
     *  @return String form of line at a given offset, null if offset out
     *          of range.
     */
	public String getLineAt( int offset ) {
		return( (String)textLines.elementAt( offset ));
	}

    /**
     *  Get the vector of Strings.
     *
     *  @return the vector of Strings
     */
	public Vector getLines() {
		return( textLines );
	}
}



