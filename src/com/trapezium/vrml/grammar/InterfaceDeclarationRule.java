/*
 * @(#)InterfaceDeclarationRule.java
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
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.fields.Field;

/**
 *  Creates an interface field scene graph component.
 *  
 *  The "interfaceDeclaration" handles exposedField, field, eventIn,
 *  and eventOut interface declarations.  field, eventIn, and eventOut
 *  declarations are handled by the RestrictedInterfaceDeclarationRule.
 *
 *  Grammar handled by "Build" method:
 *  <PRE>
 *  interfaceDeclaration:
 *
 *    restrictedInterfaceDeclaration
 *    exposedField fieldType fieldId fieldValue
 *  </PRE>    
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 18 Mar 1998, added Table 7 base profile warning
 *  @version         1.1, 18 Dec 1997
 *
 *  @since           1.0
 */
class InterfaceDeclarationRule {
    RestrictedInterfaceDeclarationRule restrictedInterfaceDeclarationRule;
    FieldFactory fieldFactory;
    
    InterfaceDeclarationRule( NodeRule nodeRule ) {
        fieldFactory = new FieldFactory( nodeRule );
        restrictedInterfaceDeclarationRule = new RestrictedInterfaceDeclarationRule( nodeRule );
    }
    
    /** Create an interface  declaration and add it to the scene graph
     *
     *  @param tokenOffset   first token of the interface declaration
     *  @param v    token enumerator containing file text
     *  @param scene   scene that contains the interface
     *  @param parent  immediate parent of the interface declaration.
     */
	void Build( int tokenOffset, TokenEnumerator v, Scene scene, Node parent ) {
		GrammarRule.Enter( "InterfaceDeclarationRule.Build" );

		if ( v.sameAs( tokenOffset, "exposedField" )) {
		    Field f = fieldFactory.CreateDeclaration( VRML97.exposedField, v, scene, parent );
		    if ( f != null ) {
    			parent.addChild( f );
    			parent.addInterface( f );
    			if ( parent.getInterfaceCount( f.getInterfaceType() ) == ( Table7.InterfaceLimit + 1 )) {
    			    f.setError( "Nonconformance, base profile " + VRML97.getInterfaceTypeStr( f.getInterfaceType() ) + " limit of " + Table7.InterfaceLimit + " exceeded here" );
    			}
    		}
		} else {
			restrictedInterfaceDeclarationRule.Build( tokenOffset, v, scene, parent );
		}
		GrammarRule.Exit( "InterfaceDeclarationRule.Build" );
	}
}
