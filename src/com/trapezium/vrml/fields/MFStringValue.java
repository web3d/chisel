/*
 * @(#)MFStringValue.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.fields;

import com.trapezium.parse.TokenEnumerator;
import com.trapezium.vrml.Scene;
import com.trapezium.vrml.VrmlElement;

/**
 *  Scene graph component for sequence of String values.
 *  
 *  In this case, all values are added to scene graph.  
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 15 Jan 1998
 *
 *  @since           1.0
 */
public class MFStringValue extends MFFieldValue {
	public MFStringValue() {
		super( -1 );
	}

	public MFStringValue( int tokenOffset, TokenEnumerator v ) {
		super( tokenOffset );
		init( tokenOffset, v, null );
	}

	public VrmlElement subclassFactory( int tokenOffset, TokenEnumerator v, Scene scene ) {
		return( new SFStringValue( tokenOffset, v ));
	}
}
