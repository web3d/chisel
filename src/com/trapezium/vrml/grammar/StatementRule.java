/*
 * @(#)StatementRule.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.grammar;

import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.Scene;
import com.trapezium.parse.TokenEnumerator;
import com.trapezium.vrml.node.PROTO;
import com.trapezium.vrml.node.Node;

/**
 *  Creates the scene graph component for a node, PROTO, or ROUTE.
 *
 *  Grammar handled by "Build" method:
 *  <PRE>
 *   statement ::=
 *        nodeStatement |
 *        protoStatement |
 *        routeStatement ;
 *
 *   protoStatement ::= 
 *        proto | 
 *        externproto ; 
 *  </PRE>    
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 24 April 1998, NodeRule parameter for autodef
 *  @version         1.1, 16 Dec 1997
 *
 *  @since           1.0
 */
class StatementRule {
    PROTORule protoRule;
    EXTERNPROTORule externProtoRule;
    ROUTERule routeRule;
    NodeStatementRule nodeStatementRule;
    
    /** Constructor */
    StatementRule( NodeRule nodeRule ) {
        externProtoRule = new EXTERNPROTORule( nodeRule );
        protoRule = nodeRule.getPROTORule();
        routeRule = nodeRule.getROUTERule();
        nodeStatementRule = new NodeStatementRule( nodeRule );
    }
    
	/**
	 * Factory/Builder method for adding a Statement to a Scene.
	 */
	void Build( int tokenOffset, TokenEnumerator v, Scene scene ) {
		Build( tokenOffset, v, scene, null );
	}

	void Build( int tokenOffset, TokenEnumerator v, Scene scene, VrmlElement parent ) {
		GrammarRule.Enter( "StatementRule.Build" );

		// no more tokens means nothing left to build
		if ( tokenOffset != -1 ) {
			if ( v.sameAs( tokenOffset, "PROTO" )) {
				protoRule.Build( tokenOffset, v, scene, parent );
			} else if ( v.sameAs( tokenOffset, "EXTERNPROTO" ) || v.matches( tokenOffset, "EXTERNPROTO#" )) {
				externProtoRule.Build( tokenOffset, v, scene, parent );
			} else if ( v.sameAs( tokenOffset, "ROUTE" )) {
				routeRule.Build( tokenOffset, v, scene, parent );
			} else {
				nodeStatementRule.Build( tokenOffset, v, scene, parent );
			}
		}

		GrammarRule.Exit( "StatementRule.Build" );
	}
}
