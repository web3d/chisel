/*
 * @(#)BuiltInNodeRule.java
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
import com.trapezium.vrml.LeftBrace;
import com.trapezium.vrml.RightBrace;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.node.NodeType;
import com.trapezium.vrml.node.DEFUSENode;
import com.trapezium.vrml.node.PROTO;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.FieldValue;
import com.trapezium.vrml.fields.MFNodeValue;


/**
 *  Creates built in VRML node scene graph components.
 *
 *  Grammar handled by "Build" method:
 *  <PRE>
 *    builtInNodeTypeId { builtInNodeGuts }
 *  </PRE>
 *  Grammar handled by "BuildNodeGuts" method:
 *  <PRE>
 *    builtInNodeGuts ::=
 *       nodeBodyElement |
 *       nodeBodyElement nodeBody |
 *       empty ;
 *  </PRE>
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 24 April 1998, added DEFNameFactory control
 *  @version         1.1, 8 Jan 1998
 *
 *  @since           1.0
 */
class BuiltInNodeRule {
    NodeBodyRule nodeBodyRule;
    DEFNameFactory defNameFactory;

    /** class constructor */
    BuiltInNodeRule( NodeRule nodeRule ) {
        nodeBodyRule = new NodeBodyRule( nodeRule );
        defNameFactory = nodeRule.getDEFNameFactory();
    }

    /** Set the DEFNameFactory, used when scene factory overrides parser factory */
    void setDEFNameFactory( DEFNameFactory defNameFactory ) {
        this.defNameFactory = defNameFactory;
    }

    /** Create a built in node and add to Scene graph.
     *
     *  @param  nodeType  built in node type
     *  @param  tokenOffset  first token of node
     *  @param  v  TokenEnumerator containing file text
     *  @param  scene  Scene containing the resulting node
     *  @param  parent immediate parent of resulting node
     */
	void Build( String nodeType, int tokenOffset, TokenEnumerator v, Scene scene, VrmlElement parent ) {
		GrammarRule.Enter( "BuiltInNodeRule.Build" );
		try {
    		Node node = VRML97.NodeFactory( nodeType );
    		VrmlElement parentElement = parent;

    		// three conditions required for auto DEFfing:
    		// 1. defNameFactory non-null
    		// 2. parent not already a DEF
    		// 3. defNameFactory returns non-null
    		if (( defNameFactory != null ) && !( parent instanceof DEFUSENode )) {
    		    String defName = defNameFactory.createDEFName( node.getNodeName() );
    		    // we assume factory handles details of generating unique name
    		    if ( defName != null ) {
    		        v.insert( tokenOffset, "DEF", defName );
            		DEFUSENode d = new DEFUSENode( tokenOffset, v, DEFUSENode.DEF );
            		tokenOffset = v.getNextToken();
            		parent.addChild( d );
            		parentElement = d;
            		scene.registerDEF( d );
            	}
            }

    		node.setFirstTokenOffset( tokenOffset );
    		parentElement.addChild( node );
    		BuildNodeGuts( node, tokenOffset, v, scene );
    		if ( parentElement instanceof DEFUSENode ) {
    		    parentElement.setLastTokenOffset( node.getLastTokenOffset() );
    		}
    		if ( parent instanceof DEFUSENode ) {
    		    parent = parent.getParent();
    		}
    		if ( parent instanceof Scene ) {
    		    parent = parent.getParent();
    		    if ( !( parent instanceof PROTO ) && NodeType.isBadChild( nodeType )) {
    		        node.setError( "Invalid child node" );
    		    }
    		} 
    		if ( NodeType.isGroupingNode( nodeType )) {
    		    Field children = node.getField( "children" );
    		    if ( children == null ) {
    		        if ( nodeType.compareTo( "Switch" ) == 0 ) {
    		            children = node.getField( "choice" );
    		        }
    		    }
    		    if ( children != null ) {
    		        FieldValue childnodes = children.getFieldValue();
    		        if ( childnodes instanceof MFNodeValue ) {
    		            MFNodeValue mfn = (MFNodeValue)childnodes;
    		            mfn.validateChildren();
    		        }
    		    }
    		}
    	} catch ( Exception e ) {
    	    System.out.println( "Exception " + e.toString() );
    	    e.printStackTrace();
    	}
        GrammarRule.Exit( "BuiltInNodeRule.Build" );
    }

	/**
	 *  Create node body, and add to node.
	 *  <P>
	 *  Node body is assumed to be:  "{  ..node body.. }"
	 *  <P>
	 *  If left brace is missing, record the error, then continue parsing
	 *  as if it were there.
	 *  <P>
	 *  If right bracket encountered where right brace might be expected,
	 *  report the error, keep token enumerator pointing to right bracket.
	 *
	 *  @param  node  Node that is being created, either a PROTOInstance or built in node
	 *  @param  tokenOffset  first token of the node guts, should be a "{"
	 *  @param  v     TokenEnumerator containing file text
	 *  @param  scene Scene containing the node
	 */
    void BuildNodeGuts( Node node, int tokenOffset, TokenEnumerator v, Scene scene ) {
		int state = v.getState();
		tokenOffset = v.getNextToken();
		boolean leftBraceError = false;
		if ( !LeftBrace.isValid( tokenOffset, v )) {
    		node.addChild( new LeftBrace( tokenOffset, v ));
    		leftBraceError = true;
    	}

		while ( true ) {
			if ( !leftBraceError ) {
				state = v.getState();
				tokenOffset = v.getNextToken();
			}
			leftBraceError = false;
			if ( tokenOffset == -1 ) {
				node.setError( "Did not find matching right brace" );
				break;
			}
			// If we get "]", add it and report as error, but leave enumerator on it as well
			if ( v.isRightBracket( tokenOffset )) {
				v.setState( state );
				break;
			}
			if ( v.isRightBrace( tokenOffset)) {
				break;
			}
			nodeBodyRule.Build( tokenOffset, v, scene, node );
		}
		if ( !RightBrace.isValid( tokenOffset, v )) {
    		node.addChild( new RightBrace( tokenOffset, v ));
    	}
		node.setLastTokenOffset( tokenOffset );

		// have to preserve state of token enumerator, verify may change it
		int vstate = v.getState();
		NodeType.verify( node, node.getNodeName(), scene );
		v.setState( vstate );
	}
}
