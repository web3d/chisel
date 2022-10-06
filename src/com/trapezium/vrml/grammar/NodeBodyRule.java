/*
 * @(#)NodeBodyRule.java
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
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.Scene;

/**
 *  Creates the scene graph component representing the body of a node.
 *  
 *  Grammar handled by "Build" method:
 *  <PRE>
 *  nodeBody ::=
 *         nodeBodyElement |
 *         nodeBodyElement nodeBody |
 *         empty ;
 *  </PRE>    
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 2 April 1998, make class public
 *  @version         1.1, 18 Dec 1997
 *
 *  @since           1.0
 */
public class NodeBodyRule {
    PROTORule protoRule;
    ROUTERule routeRule = new ROUTERule();
    FieldFactory fieldFactory;
    
    public NodeBodyRule( NodeRule nodeRule ) {
        fieldFactory = new FieldFactory( nodeRule );
        protoRule = nodeRule.getPROTORule();
    }

	/** Build onto a Node by adding on individual NodeGuts */
	public void Build( int tokenOffset, TokenEnumerator v, Scene scene, Node parent ) {
		GrammarRule.Enter( "NodeBodyRule.Build" );
		if ( v.sameAs( tokenOffset, "PROTO" )) {
			protoRule.Build( tokenOffset, v, scene, parent );
		} else if ( v.sameAs( tokenOffset, "ROUTE" )) {
			routeRule.Build( tokenOffset, v, scene, parent );
		} else {
		    parent.addChild( fieldFactory.CreateInstance( tokenOffset, v, scene, parent ));
		}
		GrammarRule.Exit( "NodeBodyRule.Build" );
	}
}
