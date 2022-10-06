/*
 * @(#)NodeStatementRule.java
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
import com.trapezium.parse.TokenEnumerator;


/**
 *  Creates the scene graph component for a node
 *  
 *  This is the only part of the grammar other than the VRML97parser
 *  that is publicly accessible.  This is necessary because SFNodeValue
 *  and MFNodeValue in the com.trapezium.vrml.fields package
 *  use an instance of this to create node scene graph components.
 *
 *  Grammar handled by "Build" method:
 *  <PRE>
 *   nodeStatement ::=
 *        node |
 *        DEF nodeNameId node |
 *        USE nodeNameId ;
 *  </PRE>
 *   The portion "DEF nodeNameId node" is handled by DEFRule.java
 *   The portion "USE nodeNameId" is handled by USERule.java
 *           
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 24 April 1998, NodeRule parameter for autodef
 *  @version         1.1, 12 Dec 1997
 *
 *  @since           1.0
 */
public class NodeStatementRule {
    DEFRule defRule;
    USERule useRule;
    NodeRule nodeRule;
    
    /** Constructor */
    public NodeStatementRule( NodeRule nodeRule ) {
        this.nodeRule = nodeRule;
        defRule = new DEFRule( nodeRule );
        useRule = new USERule();
    }
    
	/**
	 *  Factory/Builder method for adding a node child to a parent element.  DEFed nodes
	 *  have file scope, so register their names at the Scene level.
	 */
	public void Build( int tokenOffset, TokenEnumerator v, Scene scene, VrmlElement parent ) {
		GrammarRule.Enter( "NodeStatement" );

		if ( v.sameAs( tokenOffset, "DEF" )) {
			defRule.Build( tokenOffset, v, scene, parent );
		} else if ( v.sameAs( tokenOffset, "USE" )) {
			useRule.Build( tokenOffset, v, scene, parent );
		} else {
			nodeRule.Build( tokenOffset, v, scene, parent );
		}
		GrammarRule.Exit( "NodeStatement" );
	}
}
