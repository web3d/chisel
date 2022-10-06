/*
 * @(#)MFColorValue.java
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
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.Scene;

/**
 *  Scene graph component for MFColor field value.
 *  
 *  Value objects are added to parent only in error cases.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 15 Jan 1998
 *
 *  @since           1.0
 */
public class MFColorValue extends MFFieldValue {
	public MFColorValue() {
		super();
	}

	public VrmlElement subclassFactory( int tokenOffset, TokenEnumerator v, Scene scene ) {
		if ( !SFColorValue.valid( tokenOffset, v )) {
			SFColorValue colorValue = new SFColorValue( tokenOffset, v );
			optimizedValueCount += colorValue.numberValidChildren();
			return( colorValue );
		} else {
			optimizedValueCount += 3;
			return( null );
		}
	}
}

