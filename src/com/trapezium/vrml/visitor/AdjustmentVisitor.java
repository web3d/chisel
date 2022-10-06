/*
 * @(#)AdjustmentVisitor.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.visitor;

import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.MFStringValue;
import com.trapezium.vrml.fields.SFBoolValue;
import com.trapezium.vrml.fields.SFStringValue;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.node.PROTO;
import com.trapezium.vrml.node.PROTOInstance;
import com.trapezium.vrml.node.PROTObase;
import com.trapezium.vrml.node.DEFUSENode;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.Scene;
import com.trapezium.pattern.Visitor;

import com.trapezium.parse.TokenEnumerator;
import java.io.PrintStream;
import java.util.Hashtable;

/**
 *  Adjusts token offsets in all VrmlElements.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 29 March 1998, created for moving VrmlElements
 *                   between scene graphs
 *
 *  @since           1.12
 */
public class AdjustmentVisitor extends Visitor {
	Hashtable visited;
	int boundary;
	int amount;

    /** class constructor */
	public AdjustmentVisitor( TokenEnumerator v, int boundary, int amount ) {
		super( v );
		this.boundary = boundary;
		this.amount = amount;
		visited = new Hashtable();
	}

    /** Track visited objects, adjust boundaries if never been visited before */
	public boolean visitObject( Object a ) {
		if ( a instanceof VrmlElement ) {
		    if ( visited.get( a ) != null ) {
		        return( false );
		    }
		    visited.put( a, a );
		    ((VrmlElement)a).adjust( boundary, amount );
		}
		return( true );
	}
	

    /** print a summary of the information to a PrintStream */
	public void summary( PrintStream ps ) {
	}
}

