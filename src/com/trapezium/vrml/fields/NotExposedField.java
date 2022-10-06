/*
 * @(#)NotExposedField.java
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
 *  Scene graph component for a "field" instance or declaration.
 *
 *  Note this is a little confusing:
 *
 *        field === NotExposedField
 *        exposedField === ExposedField
 *        eventIn === EventIn
 *        eventOut === EventOut
 *  
 *  The Field.java class is the base class for NotExposedField, ExposedField,
 *  EventIn, and EventOut.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 21 Jan 1998
 *
 *  @since           1.0
 */
public class NotExposedField extends Field {
	public NotExposedField( int tokenOffset ) {
		super( tokenOffset );
	}
	public int getInterfaceType() {
	    return( VRML97.field );
	}
	public VrmlElement vrmlClone( VrmlElement protoInstance ) {
	    NotExposedField result = new NotExposedField( -1 );
	    cloneFieldValue( result, protoInstance );
	    return( result );
	}
}

