/*
 * @(#)SFNodeValue.java
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
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.Scene;
import com.trapezium.vrml.grammar.NodeStatementRule;
import com.trapezium.vrml.node.NULLNode;
import com.trapezium.vrml.node.DEFUSENode;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.node.PROTOInstance;

/**
 *  Scene graph component for an SFNode field value.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 24 April 1998, NodeStatementRule a parameter for autoDEF
 *  @version         1.1, 8 Jan 1998
 *
 *  @since           1.0
 */
public class SFNodeValue extends FieldValue {
    
    /** Class constructor */
	public SFNodeValue() {
		super();
	}

    /** Class constructor.
     *
     *  @param tokenOffset first token of the SFNode
     *  @param v data source for SFNode text 
     */
	public SFNodeValue( int tokenOffset, TokenEnumerator v ) {
		super( tokenOffset );
		init( tokenOffset, v, null );
	}

	public String getNodeName() {
		VrmlElement n = (VrmlElement)getChildAt( 0 );
		if ( n instanceof PROTOInstance ) {
		    PROTOInstance pi = (PROTOInstance)n;
		    n = pi.getPROTONodeType();
		}
		if ( n instanceof DEFUSENode ) {
			DEFUSENode dun = (DEFUSENode)n;
			if ( dun.getNode() == null ) {
				// This occurs in the error case when USEing a name that hasn't been DEFed
				return( null );
			}
			return( dun.getNode().getBaseName() );
		} else if ( n != null ) {
			return( n.getBaseName() );
		} else {
//			VrmlElement p = (VrmlElement)getParent();
//			p.dump( "I'm the parent of an SFNodeValue with no children!" );
			return( null );
		}
	}

    /** Get the Node for this SFNode */
	public Node getNode() {
		VrmlElement n = getChildAt( 0 );
		if ( n instanceof Node ) {
			return( (Node)n );
		} else {
			return( null );
		}
	}
	
	/** add node, NULL allowed */
	public void init( NodeStatementRule nodeStatementRule, int tokenOffset, TokenEnumerator v, Scene scene ) {
		if ( v.sameAs( tokenOffset, "NULL" )) {
			addChild( new NULLNode( tokenOffset ));
		} else if ( nodeStatementRule != null ) {
			nodeStatementRule.Build( tokenOffset, v, scene, this );
		}
	}
	
	public void init( int tokenOffset, TokenEnumerator v, Scene scene ) {
	    init( null, tokenOffset, v, scene );
	}
}
