/*
 * @(#)ExternInterfaceDeclarationRule.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.grammar;

import com.trapezium.parse.TokenEnumerator;
import com.trapezium.vrml.Scene;
import com.trapezium.vrml.BadFieldId;
import com.trapezium.vrml.FieldId;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.fields.FieldValue;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.ExposedField;
import com.trapezium.vrml.fields.NotExposedField;
import com.trapezium.vrml.fields.EventIn;
import com.trapezium.vrml.fields.EventOut;
import com.trapezium.vrml.node.Node;

/**
 *  Creates an interface declaration.
 *  
 *  Grammar handled by "Build" method:
 *  <PRE>
 *   externInterfaceDeclaration:
 *
 *    eventIn fieldType eventInId
 *    eventOut fieldType eventOutId
 *    field fieldType fieldId
 *    exposedField fieldType fieldId
 *  </PRE>
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 16 Mar 1998, added Table 7 base profile warning
 *  @version         1.1, 16 Dec 1997
 *
 *  @since           1.0
 */
class ExternInterfaceDeclarationRule {
    FieldFactory fieldFactory;
    
    /** class constructor */
    ExternInterfaceDeclarationRule( NodeRule nodeRule ) {
        this( nodeRule, true );
    }
    
    ExternInterfaceDeclarationRule( NodeRule nodeRule, boolean valueCreation ) {
        fieldFactory = new FieldFactory( nodeRule, valueCreation );
    }
    
    /** create an exposedField, field, eventIn, or eventOut declaration
     *  and add to scene graph.
     *
     *  This is only used by interface extensions, either PROTO or Script nodes.
     *
     *  @param tokenOffset  first token of the interface declaration
     *  @param v  TokenEnumerator containing the file text
     *  @param scene  Scene containing the declaration
     *  @param parent Node containing the declaration.
     */
	void Build( int tokenOffset, TokenEnumerator v, Scene scene, Node parent ) {
		int declarationType;
		if ( v.sameAs( tokenOffset, "exposedField" )) {
		    declarationType = VRML97.exposedField;
		} else if ( v.sameAs( tokenOffset, "field" )) {
			declarationType = VRML97.field;
		} else if ( v.sameAs( tokenOffset, "eventIn" )) {
			declarationType = VRML97.eventIn;
		} else if ( v.sameAs( tokenOffset, "eventOut" )) {
			declarationType = VRML97.eventOut;
		} else {
		    BadFieldId bfi = new BadFieldId( tokenOffset, "expected exposedField, field, eventIn, or eventOut" );
		    parent.addChild( bfi );
    		return;
		}

        // create the field and add it as a child, and to the node interface
	    Field f = fieldFactory.CreateDeclaration( declarationType, v, scene, parent );
	    if ( f != null ) {
   			parent.addChild( f );
   			parent.addInterface( f );
   			if ( parent.getInterfaceCount( f.getInterfaceType() ) == ( Table7.InterfaceLimit + 1 )) {
   			    f.setError( "Nonconformance, base profile " + VRML97.getInterfaceTypeStr( f.getInterfaceType() ) + " limit of " + Table7.InterfaceLimit + " exceeded here" );
   			}
   		}
	}
}
