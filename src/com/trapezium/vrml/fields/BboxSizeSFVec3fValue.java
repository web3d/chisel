/*
 * @(#)NonNegativeSFVec3fValue.java
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
import com.trapezium.vrml.Value;
import com.trapezium.vrml.VrmlElement;

/**
 *  Scene graph component for sequence of positive or all -1 vec3f values,
 *  convention used only for bboxSize fields.
 *
 *  There is no corresponding VRML97 data type for this case, however,
 *  the VRML specification does require this as a data type for some
 *  fields (e.g. scale).
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 8 Dec 1997
 *
 *  @since           1.0
 */
public class BboxSizeSFVec3fValue extends SFVec3fValue {
	public BboxSizeSFVec3fValue() {
		super();
	}

	public BboxSizeSFVec3fValue( int tokenOffset, TokenEnumerator v ) {
		super( tokenOffset, v );
	}

	public void init( int tokenOffset, TokenEnumerator v, Scene scene ) {
	    initByType( tokenOffset, v, BboxSizeValues );
	}
}
