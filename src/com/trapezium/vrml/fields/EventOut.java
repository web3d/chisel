/*
 * @(#)EventOut.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.fields;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.grammar.VRML97;
import com.trapezium.vrml.node.PROTOInstance;

import java.util.Vector;

/**
 *  Scene graph component for EventOut declaration or instance.
 *  
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 17 Dec 1997
 *
 *  @since           1.0
 */
public class EventOut extends Field {
	Vector connectedTo;

	public EventOut( int tokenOffset ) {
		super( tokenOffset );
		connectedTo = null;
	}
	
	public int getInterfaceType() {
	    return( VRML97.eventOut );
	}

	public int getConnectedToCount() {
		if ( connectedTo == null ) {
			return( 0 );
		} else {
			return( connectedTo.size() );
		}
	}

	public VrmlElement vrmlClone( VrmlElement protoInstance ) {
	    EventOut result = new EventOut( -1 );
	    cloneFieldValue( result, protoInstance );
	    return( result );
	}
}

