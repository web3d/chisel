/*
 * @(#)WildCardFilter.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.util;

import java.io.FilenameFilter;
import java.io.File;

/**
 *  Limited support for wild card file names on the command line.
 */
public class WildCardFilter implements FilenameFilter {
	String filter;

    /** Class constructor */
	public WildCardFilter( String filter ) {
		this.filter = filter;
	}

    /**
     *  Does the string indicate a wild card?  Checks for '*' or '?'
     *
     *  @param   s  string to check
     *  @return     true if the string contains a "*" character, otherwise false
     */
	static public boolean isWild( String s ) {
		if ( s.indexOf( "*" ) >= 0 ) {
			return( true );
		} else if ( s.indexOf( "?" ) >= 0 ) {
		    return( true );
        } else {
			return( false );
		}
	}

    /** Does the filter accept a file with a specific name?
     *
     *  @param dir unused, required by FilenameFilter interface
     *  @param name the name of the file to filter
     *  @return true if the name is acceptable, otherwise false.
     */
	public boolean accept( File dir, String name ) {
	    // index into filter, match character by character
		int filterIdx = 0;

		for ( int i = 0; i < name.length(); i++ ) {
		    // if the filter accepts the name character
			if ( filterAccepts( name.charAt( i ), filterIdx )) {
			    // if it's accepted because of the wild card indicator, check if
			    // the next character is accepted.  If it is, we move pass the
			    // wild card character
			    if ( filterIsQuestion( filterIdx )) {
			        filterIdx++;
			    } else if ( filterIsWild( filterIdx )) {
					if ( filterAccepts( name.charAt( i ), filterIdx + 1 )) {
						filterIdx += 2;
					}
				} else {
					filterIdx++;
				}
			} else if ( filterIsWild( filterIdx )) {
				filterIdx++;
				if ( !filterAccepts( name.charAt( i ), filterIdx )) {
					return( false );
				}
			} else {
				return( false );
			}
		}
		if ( filterIdx == filter.length() ) {
			return( true );
		} else if (( filterIdx == ( filter.length() - 1 )) && filterIsWild( filterIdx )) {
			return( true );
		} else {
			return( false );
		}
	}

    /** Check if the character at the indicated offset in the filter is a wild card
     *
     *  @param filterIdx the offset into the filter to check
     *  @return true if the indicated offset is within range and '*', otherwise false
     */
	boolean filterIsWild( int filterIdx ) {
	    if (( filter == null ) || ( filterIdx >= filter.length() )) {
	        return( false );
	    }
		return( filter.charAt( filterIdx ) == '*' );
	}

	/** Check if the character at the indicated offset is a single character wild card
	 *
	 *  @param filterIdx the offset into the filter to check
	 *  @return true if the character is within range and '?' otherwise false
	 */
	boolean filterIsQuestion( int filterIdx ) {
	    if (( filter == null ) || ( filterIdx >= filter.length() )) {
	        return( false );
	    }
		return( filter.charAt( filterIdx ) == '?' );
	}

    /** Check if the filter at its specific offset accepts a particular character.
     *  The filter accepts the character if it is identical, or the indicated filter
     *  character is a "*"
     *
     *  @param x the character to check
     *  @param filterIdx the current offset into the filter
     *
     *  @return true if the filter accepts the character, otherwise false.
     */
	boolean filterAccepts( char x, int filterIdx ) {
		if ( filterIdx < filter.length() ) {
			if ( x == filter.charAt( filterIdx )) {
				return( true );
			} else if ( filter.charAt( filterIdx ) == '*' ) {
				return( true );
			} else if ( filter.charAt( filterIdx ) == '?' ) {
			    return( true );
			}
		}
		return( false );
	}
}
