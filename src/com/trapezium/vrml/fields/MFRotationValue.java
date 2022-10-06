/*
 * @(#)MFRotationValue.java
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
 *  Scene graph component for sequence of rotation values.
 *
 *  Value objects are not added to scene graph, unless they contain errors.
 *  
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 15 Jan 1998
 *
 *  @since           1.0
 */
public class MFRotationValue extends MFFieldValue {
	public MFRotationValue() {
		super();
	}

	public VrmlElement subclassFactory( int tokenOffset, TokenEnumerator v, Scene scene ) {
		if ( !SFRotationValue.valid( tokenOffset, v )) {
			SFRotationValue srv = new SFRotationValue( tokenOffset, v );
			optimizedValueCount += srv.numberValidChildren();
			return( srv );
		} else {
			optimizedValueCount += 4;
			return( null );
		}
	}
}


