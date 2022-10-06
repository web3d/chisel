/*
 * @(#)Spelling.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.grammar;

/**
 *  Static public methods for guessing at intended spelling of words.
 *
 *  This is used by the parsing to substitute in valid field/node names
 *  when invalid ones are encountered in parsing.  Parsing then continues
 *  as if the valid name were found.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 7 Jan 1998
 *
 *  @since           1.0
 */
public class Spelling {
	//
	// simple distance between two strings,
	// exact match at a position=100
	// exact match at one off position=60
	//
	// higher score is a better match
	//
	static public int getMatchScore( String goodStr, String badStr ) {
		int score = 0;
		String goodString = goodStr.toLowerCase();
		String badString = badStr.toLowerCase();
		int minimumScore = consonantCount( goodString )*60;
		StringBuffer sb = new StringBuffer( goodString );
		int goodStringLength = goodString.length();
		for ( int i = 0; i < goodStringLength; i++ ) {
			if ( invalidIdx( badString, i )) {
				continue;
			}
			if ( isVowel( sb.charAt( i ))) {
				continue;
			}
			if ( sb.charAt( i ) == badString.charAt( i )) {
				score += 100;
				sb.setCharAt( i, '.' );
			}
		}
		for ( int i = 0; i < goodStringLength; i++ ) {
			if ( invalidIdx( badString, i + 1 )) {
				continue;
			}
			if ( isVowel( sb.charAt( i ))) {
				continue;
			}
			if ( sb.charAt( i ) == badString.charAt( i + 1 )) {
				score += 60;
				sb.setCharAt( i, '.' );
			}
		}
		for ( int i = 0; i < goodStringLength; i++ ) {
			if ( invalidIdx( badString, i - 1 )) {
				continue;
			}
			if ( isVowel( sb.charAt( i ))) {
				continue;
			}
			if ( sb.charAt( i ) == badString.charAt( i - 1 )) {
				score += 60;
				sb.setCharAt( i, '.' );
			}
		}
		if ( score < minimumScore ) {
			return( 0 );
		}
		return( score );
	}

    //
    //  the threshhold for spelling matches is the boundary value between
    //  a reasonable match and an unreasonable match.  Now set to average
    //  value of 50 per character.
    //
    static public int Threshhold( String s ) {
        if ( s == null ) {
            return( 0 );
        } else {
            return( s.length() * 40 );
        }
    }
    
	static public boolean invalidIdx( String s, int idx ) {
		if ( idx < 0 ) return( true );
		if ( idx >= s.length() ) return( true );
		return( false );
	}

	static public boolean isVowel( char x ) {
		if (( x == 'a' ) || ( x == 'e' ) || ( x == 'i' ) || ( x == 'o' ) || ( x == 'u' )) {
			return( true );
		} else if (( x == 'A' ) || ( x == 'E' ) || ( x == 'I' ) || ( x == 'O' ) || ( x == 'U' )) {
			return( true );
		} else {
			return( false );
		}
	}

	static public int consonantCount( String s ) {
		int count = 0;
		int slen = s.length();
		for ( int i = 0; i < slen; i++ ) {
			char x = s.charAt( i );
			if ( isVowel( x )) {
				continue;
			}
			count++;
		}
		return( count );
	}
}
