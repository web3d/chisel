/*
 * @(#)EventIn.java
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

/**
 *  Scene graph component for EventIn declaration or instance.
 *  
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 17 Dec 1997
 *
 *  @since           1.0
 */
public class EventIn extends Field {
	public EventIn( int tokenOffset ) {
		super( tokenOffset );
	}
	public int getInterfaceType() {
	    return( VRML97.eventIn );
	}
	public VrmlElement vrmlClone( VrmlElement pi ) {
	    EventIn result = new EventIn( -1 );
	    cloneFieldValue( result, pi );
	    return( result );
	}
}

