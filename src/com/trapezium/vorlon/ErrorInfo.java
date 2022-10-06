/*
 * @(#)ErrorInfo.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */

package com.trapezium.vorlon;

import com.trapezium.pattern.Visitor;
import com.trapezium.util.StringUtil;
import com.trapezium.parse.TokenEnumerator;
import java.io.PrintStream;

/**
 *  Information about the syntax errors and warnings encountered in a file.
 *
 *  ErrorInfo is a linked list, each link contains information about a single line.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.0, 30 Dec 1997
 *
 *  @since           1.0
 */

public class ErrorInfo {
	String line;
	String error;
	int lineNumber;
	public int offset;
	ErrorInfo next;
	ErrorInfo nextLink;

    /** class constructor
     *
     *  @param   line   the line containing the error
     *  @param   error  the String describing the error
     *  @param   lineNumber  the line number of the error
     *  @param   offset      the offset within the line of the error
     */
	public ErrorInfo( String error, int lineNumber, int offset ) {
		line = null;
		this.error = error;
		this.lineNumber = lineNumber;
		this.offset = offset;
		next = null;  // next error on same line
		nextLink = null;  // next error on another line
	}

    /** Get the next ErrorInfo in the set for this line */
	public ErrorInfo getNextLink() {
		return( nextLink );
	}

	public void setNextLink( ErrorInfo l ) {
		nextLink = l;
	}

	public void replaceLintError( ErrorInfo oldError, ErrorInfo newError ) {
		ErrorInfo scanner = this;
		while ( scanner != null ) {
			if ( scanner.getNextLink() == oldError ) {
				scanner.setNextLink( newError );
				return;
			} else {
				scanner = scanner.getNextLink();
			}
		}
	}

	public void addLink( ErrorInfo l ) {
		ErrorInfo scanner = this;
		while ( scanner != null ) {
			if ( scanner.getNextLink() == null ) {
				scanner.setNextLink( l );
				return;
			} else {
				scanner = scanner.getNextLink();
			}
		}
	}

	public int getLineNumber() {
		return( lineNumber );
	}

    public int getOffset() {
        return( offset );
    }

	/**
	 *  How many errors?
	 *
	 *  @param includeWarnings if true, warnings are also included in count
	 *
	 *  @return number of errors, and if "includeWarnings" true, also number of warnings
	 */

	public int getNumberErrors( boolean includeWarnings ) {
	    if ( includeWarnings ) {
            int errorCount = 1;
    		ErrorInfo scanner = this;
	
    		while ( scanner.next != null ) {
    			errorCount++;
    			scanner = scanner.next;
    		}
    		return( errorCount );
    	} else {
    		int errorCount = 0;
    		ErrorInfo scanner = this;
    		while ( scanner != null ) {
        		if ( scanner.error.indexOf( "Warning" ) == -1 ) {
       		        errorCount++;
       		    }
    			scanner = scanner.next;
    		}
    		return( errorCount );
    	}
	}
	
	public void updateOffsets( int adjustment ) {
		ErrorInfo scanner = next;
		while ( scanner != null ) {
			scanner.offset += adjustment;
			scanner = scanner.next;
		}
	}

    /** Get a particular error if there are several on this line */
	public ErrorInfo getError( int errNo ) {
		int counter = 0;
		ErrorInfo scanner = this;
		while ( counter != errNo ) {
			scanner = scanner.next;
			counter++;
		}
		return( scanner );
	}

	public String getError() {
		return( error );
	}

	public ErrorInfo getNextError() {
		return( next );
	}

	public boolean append( ErrorInfo le ) {
		ErrorInfo scanner = this;
		ErrorInfo prev = null;
		if ( scanner.error.compareTo( le.error ) == 0 ) {
			return( false );
		}
		while ( scanner != null ) {
			if ( scanner.error.compareTo( le.error ) == 0 ) {
				return( false );
			}
			if ( le.offset < scanner.offset ) {
				le.next = scanner;
				if ( prev != null ) {
					prev.next = le;
				}
				return( true );
			} else {
				prev = scanner;
				scanner = scanner.next;
			}
		}
		prev.next = le;
		return( true );
	}

	static public String stripTabs( String s ) {
        char[] sb = s.toCharArray();
        boolean foundtab = false;
		for ( int i = 0; i < sb.length; i++ ) {
			if ( sb[ i ] == '\t' ) {
				sb[i] = ' ';
				foundtab = true;
			}
		}
		if ( foundtab ) {
    		return( new String( sb ));
    	} else {
    	    return( s );
    	}
	}

    /** Very long line optimization requires us to know about line repeats */
	public void showError( TokenEnumerator dataSource, int lineNo, PrintStream ps ) {
		// report line numbers as 1 based, internally they are always zero based
		String lineNumberStr = new String(( lineNumber + 1 ) + ": " );
		if ( line == null ) {
		    line = dataSource.getLineAt( lineNo );
		}
		String lineStr = lineNumberStr + stripTabs( line );
		showError( lineNumberStr, lineStr, ps );
		ErrorInfo scanner = next;
		while ( scanner != null ) {
		    if ( scanner.getOffset() > 200 ) {
		        ps.println( "(offset " + scanner.getOffset() + "): " + scanner.error );
		        break;
		    }
    		scanner.showError( lineNumberStr, lineStr, ps );
			scanner = scanner.getNextError();
		}
	}
	
    void showError( String lineNumberStr, String lineStr, PrintStream ps ) {
		ps.println( lineStr );
		int len = lineNumberStr.length();
		for ( int i = 0; i < len + offset; i++ ) {
		    ps.print( ' ' );
		}
		if (( error.indexOf( "Warning" ) != 0 ) && ( error.indexOf( "Nonconformance" ) != 0 )) {
		    ps.print( "^ Error, " );
		} else {
		    ps.print( "^ " );
		}
		ps.println( error );
	}
	
	/** Get the error string for display in viewer */
	public String getDisplayString() {
	    // don't show anything too far out to right
	    if ( offset > 900 ) {
	        return( null );
	    }
	    int len = offset;
		if (( error.indexOf( "Warning" ) != 0 ) && ( error.indexOf( "Nonconformance" ) != 0 )) {
		    len += 9;
		} else {
		    len += 2;
		}
		len += error.length();
		StringBuffer sb = new StringBuffer( len );
		for ( int i = 0; i < offset; i++ ) {
		    sb.append( ' ' );
		}
		if (( error.indexOf( "Warning" ) != 0 ) && ( error.indexOf( "Nonconformance" ) != 0 )) {
		    sb.append( "^ Error, " );
		} else {
		    sb.append( "^ " );
		}
		sb.append( error );
		return( new String( sb ));
	}

    /** Get the error string for display on status line */
	public String getStatusString() {
		StringBuffer sb = new StringBuffer( error.length() + 7 );
		if (( error.indexOf( "Warning" ) != 0 ) && ( error.indexOf( "Nonconformance" ) != 0 )) {
		    sb.append( "Error, " );
		}
		sb.append( error );
		return( new String( sb ));
	}
}
