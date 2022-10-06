/*
 * @(#)USERule.java
 *
 * Copyright (c) 1998-1999 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.grammar;

import com.trapezium.parse.TokenEnumerator;
import com.trapezium.vrml.node.DEFUSENode;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.Scene;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.node.PROTOInstance;

/**
 *  Creates the scene graph component for a USE node.
 *
 *  Grammar handled by "Build" method:
 *  <PRE>
 *    USE nodeNameId
 *  </PRE>    
 *  @author          Johannes N. Johannsen
 *  @version         1.4, 10 Feb 1999, USE checks for looping ref to parent
 *                   1.1, 21 Jan 1998
 *
 *  @since           1.0
 */
class USERule {
	void Build( int tokenOffset, TokenEnumerator v, Scene scene, VrmlElement parent ) {
		GrammarRule.Enter( "USERule.Build" );
		// at this point, we know tok is "USE", get next one
		DEFUSENode u = new DEFUSENode( tokenOffset, v, DEFUSENode.USE );
		parent.addChild( u );
		u.setLastTokenOffset( v.getCurrentTokenOffset() );
		// look up the "DEF" in the scene
		DEFUSENode n = scene.getDEF( u );
		if ( n == null ) {
			u.setError( "No DEF for " + u.getId() );
		} else {
			n.markUsed();
			VrmlElement scanner = u;
			scanner = scanner.getParent();
			if ( scanner != null ) {
			    scanner = scanner.getParent();
			}
			VrmlElement grandParent = scanner;
			boolean checkUSEref = true;
			if ( grandParent == null ) {
			    checkUSEref = false;
			} else if ( grandParent instanceof PROTOInstance ) {
			    checkUSEref = false;
			} else if ( grandParent.getBaseName().compareTo( "Script" ) == 0 ) {
			    checkUSEref = false;
			}
			boolean loopingReference = false;
			while ( scanner != null ) {
			    if ( scanner == n ) {
			        if ( checkUSEref ) {
       			        u.setError( "USE referring to parent node" );
       			    }
   			        loopingReference = true;
			        break;
			    }
			    scanner = scanner.getParent();
			}
			Node actualNode = n.getNode();
			if ( actualNode == null ) {
				u.setError( "DEF node has no node" );
			} else {
			    // only add child if its not a loop back to self.
			    // to prevent infinite loop in visitors
			    if ( !loopingReference ) {
    				u.addChild( actualNode );
    			}
				if ( actualNode.isBindable() ) {
				    u.setError( "Warning, possibly undefined behavior using bindable node" );
				}
			}
		}
		GrammarRule.Exit( "USERule.Build" );
	}
}
