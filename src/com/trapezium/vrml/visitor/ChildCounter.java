/*
 * @(#)ChildCounter.java
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

import com.trapezium.parse.TokenEnumerator;
import com.trapezium.vrml.LeftBrace;
import com.trapezium.vrml.LeftBracket;
import com.trapezium.vrml.RightBrace;
import com.trapezium.vrml.RightBracket;

/**
 *  Counts all children that aren't brackets or braces.
 *
 *  Probably should get rid of this, can be done more simply.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 31 Oct 1997
 *
 *  @since           1.0
 */
public class ChildCounter extends Visitor {
	int childCount;

    /** class constructor */
	public ChildCounter( TokenEnumerator v ) {
		super( v );
		childCount = 0;
	}

    /** visit an object, only down one level.
     *  <P>
     *  "visitLevel" is handled by Visitor class, is incremented each
     *  time we drop down a level in the scene graph, and is decremented
     *  each time we go up a level in the scene graph.
     */
	public boolean visitObject( Object a ) {
		if ( visitLevel == 1 ) {
			return( true );
		} else {
			if ( !(( a instanceof LeftBrace ) || ( a instanceof RightBrace ) ||
				  ( a instanceof LeftBracket ) || ( a instanceof RightBracket ))) {
				childCount++;
			}
			return( false );
		}
	}

    /** how many children VrmlElements were found? */
	public int getChildCount() {
		return( childCount );
	}
}

