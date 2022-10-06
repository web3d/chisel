/*
 * @(#)RestrictedInterfaceDeclarationRule.java
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
import com.trapezium.vrml.fields.FieldValue;
import com.trapezium.vrml.fields.EventIn;
import com.trapezium.vrml.fields.EventOut;
import com.trapezium.vrml.fields.NotExposedField;
import com.trapezium.vrml.Scene;
import com.trapezium.vrml.fields.ISField;
import com.trapezium.vrml.BadFieldId;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.fields.Field;

/**
 *  Creates the scene graph component for a eventIn, eventOut & fields.
 *
 *  Grammar handled by "Build" method:
 *  <PRE>
 *    restrictedInterfceDeclaration:
 *      eventIn fieldType eventInId
 *      eventOut fieldType eventOutId
 *      field fieldType fieldId fieldValue
 *  </PRE>
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 28 Feb 1998, added Table7 limits
 *  @version         1.1, 6 Jan 1998
 *
 *  @since           1.0
 */
class RestrictedInterfaceDeclarationRule {
    
    FieldFactory fieldFactory;
    int interfaceLimit;
    
    RestrictedInterfaceDeclarationRule( NodeRule nodeRule ) {
        this( nodeRule, Table7.InterfaceLimit );
    }
    
    RestrictedInterfaceDeclarationRule( NodeRule nodeRule, int limit ) {
        fieldFactory = new FieldFactory( nodeRule );
        interfaceLimit = limit;
    }
    
    /** Create eventIn, eventOut, or field interface declaration and add to scene graph.
     *
     *  @param  tokenOffset  first token of the interface declaration
     *  @param  v            TokenEnumerator containing file text
     *  @param  scene        Scene containing the declaration
     *  @param  parent       immediate parent of the declaration.
	 *
	 *  @return the Field created
     */
	Field Build( int tokenOffset, TokenEnumerator v, Scene scene, Node parent ) {
		GrammarRule.Enter( "RestrictedInterfaceDeclarationRule.Build" );
		Field f = null;

		if ( v.sameAs( tokenOffset, "eventIn" )) {
		    f = fieldFactory.CreateDeclaration( VRML97.eventIn, v, scene, (Node)parent );
		    if ( f != null ) {
        		v.breakLineAt( tokenOffset );
    		    parent.addChild( f );
    		    parent.addInterface( f );
    		}
		} else if ( v.sameAs( tokenOffset, "eventOut" )) {
		    f = fieldFactory.CreateDeclaration( VRML97.eventOut, v, scene, (Node)parent );
		    if ( f != null ) {
        		v.breakLineAt( tokenOffset );
		        parent.addChild( f );
		        parent.addInterface( f );
		    }
		} else if ( v.sameAs( tokenOffset, "field" )) {
		    f = fieldFactory.CreateDeclaration( VRML97.field, v, scene, (Node)parent );
		    if ( f != null ) {
        		v.breakLineAt( tokenOffset );
		        parent.addChild( f );
		        parent.addInterface( f );
		    }
		} else {
			parent.addChild( new BadFieldId( tokenOffset, "expected eventIn, eventOut, or field" ));
		}
		if ( f != null ) {
   			if ( parent.getInterfaceCount( f.getInterfaceType() ) == ( interfaceLimit + 1 )) {
   			    f.setError( "Nonconformance, base profile " + VRML97.getInterfaceTypeStr( f.getInterfaceType() ) + " limit of " + interfaceLimit + " exceeded here" );
   			}
   		}
		GrammarRule.Exit( "RestrictedInterfaceDeclarationRule.Build" );
		return( f );
	}
}
