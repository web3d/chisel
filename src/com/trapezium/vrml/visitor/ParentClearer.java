/*
 * @(#)ParentClearer.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.visitor;

import com.trapezium.pattern.Visitor;
import com.trapezium.vrml.VrmlElement;

/**
 *  Clears all parent fields.
 *
 *  Necessary to help garbage collection, where parent-child cycles
 *  prevent object recycling.
 */
public class ParentClearer extends Visitor {
	public ParentClearer() {
	    super( null );
	}

	public boolean visitObject( Object a ) {
		if ( a instanceof VrmlElement ) {
			VrmlElement vle = (VrmlElement)a;
			vle.setParent( null );
		}
		return( true );
	}
}
