/*
 * @(#)TextLineParser.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.parse;

import java.io.*;
import java.util.*;

/**
 *  Converts InputStream into a vector of lines.  Works with 
 *  TextLineEnumerator.  No longer used with VRML files, Strings too
 *  memory intensive.  Should be removed.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.0, 31 Oct 1997
 *
 *  @since           1.0
 */
public class TextLineParser {
	Vector lines;

    /**
     *  Class constructor, creates vector of String lines from an InputStream
     *
     *  @param  inStream  an InputStream containing text
     */
	public TextLineParser( InputStream inStream ) {
		lines = new Vector( 5000, 5000 );
    	loadLines( inStream );
    }

    /**
     *  Class Constructor, create vector of String lines from a file
     *
     *  @param  source  a File containing text
     */
	public TextLineParser( File source ) throws FileNotFoundException, IOException {
		lines = new Vector( 100, 1000 );
		BufferedInputStream inStream = new BufferedInputStream( new FileInputStream( source ));
		loadLines( inStream );
	}

    /**
     *  Get the vector of Strings that were loaded from the file
     *
     *  @return  a vector of String objects
     */
	public Vector getLines() {
		return( lines );
	}

    /**
     *  Initialize the vector of string objects from text read from an InputStream
     *
     *  @param  inStream   the InputStream containing the text
     */
	void loadLines( InputStream inStream ) {
		int pushChar = 0;
		int pushChar2 = 0;
		int lineCount = 0;
		try {
			while ( true ) {
				// The StringBuffer has the contents of the line
				StringBuffer buf = new StringBuffer();

				// flag indicates whether the line was added to the list, used only
				// when end of file is reached, otherwise \n or \r result in line being 
				// added
				boolean addedBuf = false;

				// The following formats for end of line have been encountered:
				//    \r -- Late Night VRML 2.0
				//    \r\r\n -- file from michael
				//    \r\n -- vi, notepad
				//
				// General rule seems to be if there is a \n, we can ignore the \r's.
				// Problem then becomes detecting when there are \r's or not.
				int x = pushChar;
				if (( x != 0 ) && ( x != -1 )) {
					buf.append( (char)x );
				}
				if (( buf.length() == 1 ) && (( buf.charAt( 0 ) == '\n' ) || ( buf.charAt( 0 ) == '\r' ))) {
					String l = new String( buf );
					lines.addElement( l );
					lineCount++;
					buf = new StringBuffer();
				}
				x = pushChar2;
				if (( x != 0 ) && ( x != -1 )) {
					buf.append( (char)x );
				}

				pushChar = 0;
				pushChar2 = 0;
				// read the stream until:  end of file, \r or \n
				while (( x = inStream.read() ) != -1 ) {
					// we have a \r character, 
					if ( x == '\r' ) {
						String l = new String( buf );
						lines.addElement( l );
						lineCount++;
						x = inStream.read();  // skip other one
						if ( x != '\n' ) {
							// Some files have lines ending with \r\r\n!
							if ( x == '\r' ) {
								x = inStream.read();
								if ( x != '\n' ) {
									pushChar = '\r';
									pushChar2 = x;
								}
							} else {
								pushChar = x;
							}
						}
						addedBuf = true;
						break;
					}
					if ( x == '\n' ) {
						String l = new String( buf );
						lines.addElement( l );
						lineCount++;
						x = inStream.read();  // skip other one
						if ( x != '\r' ) {
							pushChar = x;
						}
						addedBuf = true;
						break;
					}
					buf.append( (char)x );
				}
				if (( x == -1 ) && !addedBuf ) {
					String l = new String( buf );
					lines.addElement( l );
					lineCount++;
					break;
				}
			}

			inStream.close();
		} catch ( Exception e ) {
			System.out.println( "Parser load lines failed: " + e );
		}
	}


    /**
     *  Debugging dump of the TextLineParser
     */
	public void dump() {
		for ( int i = 0; i < lines.size(); i++ ) {
			String s = (String)lines.elementAt( i );
			if ( s == null ) {
				System.out.println( "" );
			} else {
				System.out.println( (String)lines.elementAt( i ) );
			}
		}
	}
}


