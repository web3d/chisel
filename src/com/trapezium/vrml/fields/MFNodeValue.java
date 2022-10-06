/*
 * @(#)MFNodeValue.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.fields;

import com.trapezium.vrml.grammar.NodeStatementRule;
import com.trapezium.parse.TokenEnumerator;
import com.trapezium.vrml.node.NULLNode;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.node.NodeType;
import com.trapezium.vrml.node.DEFUSENode;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.Scene;

/**
 *  Scene graph component for sequence of Node values.
 *  
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 24 April 1998, NodeStatementRule a parameter for autoDEf
 *  @version         1.1, 15 Jan 1998
 *
 *  @since           1.0
 */
public class MFNodeValue extends MFFieldValue {
	public MFNodeValue() {
		super();
	}

    public VrmlElement subclassFactory( int tokenOffset, TokenEnumerator v, Scene scene ) {
        return( subclassFactory( null, tokenOffset, v, scene ));
    }
    
	public VrmlElement subclassFactory( NodeStatementRule nodeStatementRule, int tokenOffset, TokenEnumerator v, Scene scene ) {
		if ( v.sameAs( tokenOffset, "NULL" ) && ( numberChildren() == 0 )) {
			addChild( new NULLNode( tokenOffset ));
		} else {
			nodeStatementRule.Build( tokenOffset, v, scene, this );
			optimizedValueCount++;
		}
		return( null );
	}
	
	/* Verify that children are valid children nodes, called for all grouping nodes. */
	public void validateChildren() {
	    int n = numberChildren();
	    for ( int i = 0; i < n; i++ ) {
	        VrmlElement child = getChildAt( i );
	        if ( child instanceof Node ) {
	            Node node = (Node)child;
	            String name = node.getNodeName();
	            if ( name != null ) {
    	            if ( NodeType.isBadChild( name )) {
	                    node.setError( "Invalid child node" );
	                }
	            }
	        }
	    }
	}
	
	/** Get a particular node child */
	public Node getChildNode( int offset ) {
	    int currentOffset = 0;
	    int n = numberChildren();
	    for ( int i = 0; i < n; i++ ) {
	        VrmlElement child = getChildAt( i );
	        if ( child instanceof Node ) {
	            if ( offset == currentOffset ) {
	                if ( child instanceof DEFUSENode ) {
	                    DEFUSENode dun = (DEFUSENode)child;
	                    return( dun.getNode() );
	                } else {
	                    return( (Node)child );
	                }
	            } else {
	                currentOffset++;
	            }
	        }
	    }
	    return( null );
	}
}
