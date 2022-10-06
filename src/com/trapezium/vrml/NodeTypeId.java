/*
 * @(#)NodeTypeId.java
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
 *  Scene graph component representing a new node type defined by PROTO
 *  or EXTERNPROTO.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 3 March 1998, Table 7-1 base profile warning 50 chars
 *  @version         1.1, 3 Nov 1997
 *
 *  @since           1.0
 */
public class NodeTypeId extends SingleTokenElement {
    String name;
	public NodeTypeId( int tokenOffset, TokenEnumerator v ) {
		super( tokenOffset );
		name = v.toString( tokenOffset );
		if ( name.length() > 50 ) {
		    setError( "Nonconformance, name length " + name.length() + " exceeds base profile limit of 50" );
		}
	}
	public String getName() {
	    return( name );
	}
}

