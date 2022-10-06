/*
 * @(#)ISField.java
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
import com.trapezium.vrml.node.PROTO;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.Value;
import com.trapezium.vrml.grammar.VRML97;

/**
 *  Scene graph component for field values indicated by "IS <name>" syntax.
 *  
 *  An ISField is a connection between a PROTO interface field and a
 *  specific field in a node contained in that PROTO.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 8 Jan 1998
 *
 *  @since           1.0
 */
public class ISField extends Field {
    // IS syntax:
    //
    //    <nodeField> IS <protoField>
    //
    //  An ISField is a connection between a proto interface field
    //  and a specific field in a node
	Field protoField = null;
	Field nodeField = null;
	
	/** get the PROTO interface field (right side of "IS") */
	public Field getPROTOfield() {
		return( protoField );
	}
	
	/** get the node field (left side of "IS") */
	public Field getNodeField() {
	    return( nodeField );
	}
	
	/** get the String name of the Field defined by the IS syntax */
	public String getFieldId() {
	    if ( protoField != null ) {
	        return( protoField.getFieldId() );
	    }
	    return( null );
	}

    /** ISField constructor.
     *
     *  @param nodeField the field being defined with the IS syntax
     *  @param tokenOffset the offset of the token of the word "IS"
     *  @param v TokenEnumerator data source
     *  @param protoParent reference to the PROTO definition that the
     *    IS syntax is referencing (IS references one of the PROTO interface
     *    fields)
     */
	public ISField( Field nodeField, int tokenOffset, TokenEnumerator v, PROTO protoParent ) {
		super( tokenOffset );
		this.nodeField = nodeField;
		tokenOffset = v.getNextToken();
		if ( protoParent == null ) {
			setError( "IS must be contained in a PROTO" );
		} else if ( tokenOffset != -1 ) {
    		protoParent.addISField( this );
    		setLastTokenOffset( tokenOffset );
			Value val = new Value( tokenOffset );
			String protoInterfaceFieldName = v.toString( tokenOffset );
			VrmlElement decl = protoParent.getInterfaceDeclaration( protoInterfaceFieldName );

			if ( decl == null ) {
				decl = protoParent.getClosestInterfaceDeclaration( protoInterfaceFieldName );
				if ( decl != null ) {
					if ( decl instanceof Field ) {
						Field fdecl = (Field)decl;
						val.setError( "field not valid for PROTO " + protoParent.getId() + ", possibly '" + fdecl.getFieldId() + "'" );
					}
				} else {
					val.setError( "field not valid for PROTO " + protoParent.getId() );
				}
			}
			addChild( val );
			if ( decl != null ) {
				if ( decl instanceof Field ) {
					protoField = (Field)decl;
					
					// apply PROTO interface checks
					int protoFieldType = protoField.getInterfaceType();
					int nodeFieldType = nodeField.getInterfaceType();
			        String tid = v.toString( nodeField.getFirstTokenOffset() );
			        if ( tid.indexOf( "set_" ) == 0 ) {
			            nodeFieldType = VRML97.eventIn;
			        } else if ( tid.indexOf( "_changed" ) > 0 ) {
			            nodeFieldType = VRML97.eventOut;
			        }
					
					if ( nodeFieldType == VRML97.field ) {
					    if ( protoFieldType != VRML97.field ) {
					        nodeField.setError( "field can only connect to PROTO interface field" );
					    }
					} else if ( nodeFieldType == VRML97.exposedField ) {
					    // exposed field can connect to any interface
					} else if ( nodeFieldType == VRML97.eventIn ) {
					    if ( protoFieldType != VRML97.eventIn ) {
					        nodeField.setError( "eventIn can only connect to PROTO interface eventIn" );
					    }
					} else if ( nodeFieldType == VRML97.eventOut ) {
					    if ( protoFieldType != VRML97.eventOut ) {
					        nodeField.setError( "eventOut can only connect to PROTO interface eventOut" );
					    }
					}
				}
			}
		} else {
			setError( "Need a PROTO field name after \"IS\"" );
		}
	}
}
