/*
 * @(#)ScriptRestrictedInterfaceDeclarationRule.java
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
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.fields.ISField;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.fields.Field;

/**
 *  Creates the scene graph component for a eventIn, eventOut & fields,
 *  allows IS initializations if contained in PROTO.  Otherwise, same
 *  as restrictedInterfaceDeclaration.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 15 May 1998, make public
 *                         23 Feb 1998, added Table 7 base profile limit
 *  @version         1.11, 22 Jan 1998
 *
 *  @since           1.11
 */
public class ScriptRestrictedInterfaceDeclarationRule extends RestrictedInterfaceDeclarationRule {

    /** Constructor for Script interface declaration rule parser */
    public ScriptRestrictedInterfaceDeclarationRule( NodeRule nodeRule ) {
        super( nodeRule, Table7.ScriptInterfaceLimit );
    }

    /** Create eventIn, eventOut, or field interface declaration and add to scene graph.
     *
     *  @param  tokenOffset  first token of the interface declaration
     *  @param  v            TokenEnumerator containing file text
     *  @param  scene        Scene containing the declaration
     *  @param  parent       immediate parent of the declaration.
	 *
	 *  @return  the field created
     */
	public Field Build( int tokenOffset, TokenEnumerator v, Scene scene, Node parent ) {
		GrammarRule.Enter( "ScriptRestrictedInterfaceDeclarationRule.Build" );
		Field result = super.Build( tokenOffset, v, scene, parent );
		int state = v.getState();
		int next = v.getNextToken();
		if ( v.sameAs( next, "IS" )) {
		    VrmlElement lastChild = parent.getLastChild();
		    if (( lastChild != null ) && ( lastChild instanceof Field )) {
		        Field f = (Field)lastChild;
    			ISField isField = new ISField( f, tokenOffset, v, scene.getPROTOparent() );
    			f.addChild( isField );
				VRML97.checkISFieldTypes( isField, parent );
				result = isField;
       		}
		} else {
		    v.setState( state );
		}
		GrammarRule.Exit( "ScriptRestrictedInterfaceDeclarationRule.Build" );
		return( result );
	}
}
