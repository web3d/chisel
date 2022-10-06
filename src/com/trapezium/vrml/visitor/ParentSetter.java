/*
 * @(#)ParentSetter.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.visitor;

import com.trapezium.parse.TokenEnumerator;
import com.trapezium.pattern.Visitor;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.node.DEFUSENode;

/**
 *  Sets all parent fields.
 *
 *  Necessary to prevent infinite loop in case where Script node
 *  has a self reference.  Example:
 *  <PRE>
 *  DEF CONTROL Script {
 *      field SFNode me USE CONTROL
 *  }
 *  </PRE>
 *           
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 31 Oct 1997
 *
 *  @since           1.0
 */
public class ParentSetter extends Visitor {
	public ParentSetter( TokenEnumerator v ) {
	    super( v );
	}

	public boolean visitObject( Object a ) {
		if ( a instanceof VrmlElement ) {
			VrmlElement vle = (VrmlElement)a;
			if ( vle instanceof DEFUSENode ) {
				DEFUSENode dun = (DEFUSENode)vle;
				if ( !dun.isDEF() ) {
					return( true );
				}
			}
			for ( int i = 0; i < vle.numberChildren(); i++ ) {
				VrmlElement vle2 = vle.getChildAt( i );
				VrmlElement parent = vle2.getParent();
				if ( parent != vle ) {
					if ( vle2 instanceof DEFUSENode ) {
						DEFUSENode dun = (DEFUSENode)vle2;
						if ( !dun.isDEF() ) {
							continue;
						}
					}
					vle2.setParent( vle );
				}
			}
		}
		return( true );
	}
}
