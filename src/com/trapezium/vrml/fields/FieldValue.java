/*
 * @(#)FieldValue.java
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
import com.trapezium.vrml.MultipleTokenElement;
import com.trapezium.vrml.Scene;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.node.PROTOInstance;
import com.trapezium.vrml.grammar.NodeStatementRule;

/**
 *  Abstract base class for all field value instances.
 *  
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 17 Dec 1997
 *
 *  @since           1.0
 */
abstract public class FieldValue extends MultipleTokenElement {
	public FieldValue( int tokenOffset ) {
	    super( tokenOffset );
    }

	public FieldValue() {
		super( -1 );
	}

    public VrmlElement vrmlClone( VrmlElement protoInstance ) {
        VrmlElement ve = getChildAt( 0 );
        if ( ve instanceof ISField ) {
            ISField is = (ISField)ve;
            Field protoField = is.getPROTOfield();
            String fieldId = protoField.getFieldId();

            // now determine if fieldId is present in this PROTO instance
            PROTOInstance pi = (PROTOInstance)protoInstance;
            Field f = pi.getField( fieldId );
            if ( f == null ) {
                f = pi.getInterfaceDeclaration( fieldId );
                VrmlElement c0 = f.getChildAt( 0 );
                if ( c0 instanceof Field ) {
                    Field ff = (Field)c0;
                    FieldValue fv = ff.getFieldValue();
                    return( fv );
                }
            } else {
                FieldValue fv = f.getFieldValue();
                return( fv );
            }
            return( null );
        } else {
            return( this );
        }
    }
    
    /** init that builds on scene graph, only used by a few nodes */
	public void init( NodeStatementRule nodeStatementRule, int tokenOffset, TokenEnumerator v, Scene scene ) {
	    init( tokenOffset, v, scene );
	}
	
	/** init that adds to scene graph, but does no additional parsing of other types */
    abstract public void init( int tokenOffset, TokenEnumerator v, Scene scene );
}



