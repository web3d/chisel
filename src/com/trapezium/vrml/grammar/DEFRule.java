/*
 * @(#)DEFRule.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.grammar;

import com.trapezium.vrml.Scene;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.node.DEFUSENode;
import com.trapezium.vrml.node.Node;
import com.trapezium.parse.TokenEnumerator;

/**
 *  Creates a DEF node scene graph component.
 *  
 *  Grammar handled by "Build" method:
 *  <PRE>
 *    DEF nodeNameId node
 *  </PRE>
 *  After the node is created, its name is registered in the Scene.
 *  If any other node is registered with that same name, both are
 *  marked with "duplicate DEF" warnings.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 24 April 1998, nodeRule parameter for autodef
 *  @version         1.12, 12 March 1998, added Table 7 base profile warning
 *  @version         1.1, 12 Dec 1997
 *
 *  @since           1.0
 */
public class DEFRule {
    NodeRule nodeRule;
    
    DEFRule( NodeRule nodeRule ) {
        this.nodeRule = nodeRule;
    }
    
    /** Create DEF node scene graph components, and add to the Scene graph.
     *
     *  @param tokenOffset  token starting the "DEF...." 
     *  @param v   TokenEnumerator containing file text
     *  @param scene  Scene containing this DEF
     *  @param parent VrmlElement that is the immediate parent of this DEf
     */
	void Build( int tokenOffset, TokenEnumerator v, Scene scene, VrmlElement parent ) {
		GrammarRule.Enter( "DEFRule.Build" );

		// at this point, we know tok is "DEF", get next one
		DEFUSENode d = new DEFUSENode( tokenOffset, v, DEFUSENode.DEF );
		tokenOffset = v.getNextToken();
		parent.addChild( d );
		String defId = d.getId();
		DEFUSENode checkDef = scene.getDEF( defId );
		if ( defId != null ) {
		    if ( defId.length() > Table7.NameLimit ) {
		        d.setError( "Nonconformance, name length " + defId.length() + " exceeds base profile name length " + Table7.NameLimit );
		    }
		}
		scene.registerDEF( d );
		
		nodeRule.Build( tokenOffset, v, scene, d );
		d.setLastTokenOffset( v.getCurrentTokenOffset() );
		if ( checkDef != null ) {
			d.setError( "Warning, duplicate DEF" );
			checkDef.setError( "Warning, duplicate DEF" );
		}

		GrammarRule.Exit( "DEFRule.Build" );
	}
}
