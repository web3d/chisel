/*
 * @(#)StringUtil.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.util;

/**
 *  Some static public routines for operating on Strings.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.0, 30 Dec 1997
 *
 *  @since           1.0
 */
public class StringUtil {
	static String spacerString = "                                                                                                                 ";
	
	/** get a space string for a given level of indentation */
	static public String spacer( int level ) {
		return( spacerString.substring( 0, level ));
	}

    /** strip the quotes of a string, if any */
	static public String stripQuotes( String s ) {
		if ( s == null ) {
			return( s );
		}
		if ( s.length() == 0 ) {
			return( s );
		}
		if ( s.charAt( 0 ) == '"' ) {
		    if ( s.length() == 1 ) {
		        return( null );
		    }
			return( s.substring( 1, s.length() - 1 ));
		} else {
			return( s );
		}
	}

    /** strip all characters following and including '#' from the string */
	static public String stripPound( String s ) {
		int poundIndex = s.indexOf( '#' );
		if ( poundIndex == -1 ) {
			return( s );
		} else {
			return( s.substring( 0, poundIndex ));
		}
	}
}
