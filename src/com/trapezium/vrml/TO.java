/*
 * @(#)TO.java
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
 *  Scene graph component representing the "TO" portion of a ROUTE.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 31 Oct 1997
 *
 *  @since           1.0
 */
public class TO extends SingleTokenElement {
	public TO( int tokenOffset, TokenEnumerator v ) {
		super( tokenOffset );
		if ( !v.sameAs( tokenOffset, "TO" )) {
			setError( "Expected 'TO' at this point" );
		}
	}
}

