/*
 * @(#)LeftBrace.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml;
import com.trapezium.parse.TokenEnumerator;

/**
 *  Scene graph component representing a LeftBrace.
 *
 *  Optimization is to only store these when there is an error.  This
 *  optimization is not complete, done in some places.  When this
 *  optimization is complete, LeftBrace can be replaced by ErrorElement.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.0, 11 Dec 1997
 *
 *  @since           1.0
 */
public class LeftBrace extends SingleTokenElement {
	public LeftBrace( int tokenOffset, TokenEnumerator v ) {
		super( tokenOffset );
		if ( !v.sameAs( tokenOffset, "{" )) {
			setError( "Expected '{' at this point" );
		}
	}
	public LeftBrace( TokenEnumerator v ) {
		this( v.getNextToken(), v );
	}
	static public boolean isValid( int tokenOffset, TokenEnumerator v ) {
	    return( v.sameAs( tokenOffset, "{" ));
	}
}

