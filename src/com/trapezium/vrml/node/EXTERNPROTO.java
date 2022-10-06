/*
 * @(#)EXTERNPROTO.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.node;

import com.trapezium.vrml.Scene;
import com.trapezium.vrml.VrmlElement;

/**
 *  Scene graph component for an EXTERNPROTO declaration.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 3 Dec 1997
 *
 *  @since           1.0
 */
public class EXTERNPROTO extends PROTObase {
	public EXTERNPROTO( int tokenOffset ) {
		super( tokenOffset );
	}

	public String getBuiltInNodeType() {
		return( "EXTERNPROTO#" );
	}
}

