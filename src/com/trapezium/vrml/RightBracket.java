/*
 * @(#)RightBracket.java
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
 *  Scene graph component representing a RightBracket.
 *
 *  Optimization is to only store these when there is an error.  This
 *  optimization is not complete, done in some places.  When this
 *  optimization is complete, RightBracket can be replaced by ErrorElement.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.0, 5 Nov 1997
 *
 *  @since           1.0
 */
public class RightBracket extends SingleTokenElement {
	public RightBracket( int tokenOffset, TokenEnumerator v ) {
		super( tokenOffset );
		if (( tokenOffset == -1 ) || ( !v.sameAs( tokenOffset, "]" ))) {
			setError( "Expected ']' at this point" );
		}
	}
	static public boolean isValid( int tokenOffset, TokenEnumerator v ) {
	    return( v.sameAs( tokenOffset, "]" ));
	}
}

