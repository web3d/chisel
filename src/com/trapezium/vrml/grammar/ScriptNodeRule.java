/*
 * @(#)ScriptNodeRule.java
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
import com.trapezium.vrml.LeftBrace;
import com.trapezium.vrml.RightBrace;
import com.trapezium.parse.TokenEnumerator;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.node.ScriptInstance;
import com.trapezium.vrml.node.PROTO;
import com.trapezium.vrml.node.DEFUSENode;

/**
 *  Creates the scene graph component for a Script node.
 *
 *  Grammar handled by "Build" method:
 *  <PRE>
 *    Script { scriptGuts }
 *  </PRE>
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 24 April 1998, auto DEF
 *  @version         1.1, 18 Dec 1997
 *
 *  @since           1.0
 */
class ScriptNodeRule {

    ScriptGutRule scriptGutRule;
    DEFNameFactory defNameFactory;

    ScriptNodeRule( NodeRule nodeRule ) {
        scriptGutRule = new ScriptGutRule( nodeRule );
        defNameFactory = nodeRule.getDEFNameFactory();
    }

    /** Set the DEFNameFactory, used when scene factory overrides parser factory */
    void setDEFNameFactory( DEFNameFactory defNameFactory ) {
        this.defNameFactory = defNameFactory;
    }

	void Build( int tokenOffset, TokenEnumerator v, Scene scene, VrmlElement parent ) {
		try {
			Node scriptNode = new ScriptInstance();
			VrmlElement parentElement = parent;
    		// three conditions required for auto DEFfing:
    		// 1. defNameFactory non-null
    		// 2. parent not already a DEF
    		// 3. defNameFactory returns non-null
    		if (( defNameFactory != null ) && !( parent instanceof DEFUSENode )) {
    		    String defName = defNameFactory.createDEFName( "Script" );
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
			scriptNode.setFirstTokenOffset( tokenOffset );
			parentElement.addChild( scriptNode );
			tokenOffset = v.getNextToken();
			boolean firstTimeSkip = false;
			if ( !v.sameAs( tokenOffset, "{" )) {
    			scriptNode.addChild( new LeftBrace( tokenOffset, v ));
				firstTimeSkip = true;
			}
			int sgrcount = 0;
			int oldTokenOffset = 0;
			PROTO protoParent = getPROTOparent( parent );
			while ( true ) {
				if ( !firstTimeSkip ) {
				    oldTokenOffset = tokenOffset;
					tokenOffset = v.getNextToken();
				}
				firstTimeSkip = false;

				if ( tokenOffset == -1 ) {
					scriptNode.setError( "Unexpected end of file processing script node" );
					break;
				}
				// allow for multiline "javascript: ..."
				while ( v.isContinuationString( tokenOffset )) {
					tokenOffset = v.getNextToken();
				}
				if ( v.sameAs( tokenOffset, "}" )) {
					break;
				}
				scriptGutRule.Build( tokenOffset, v, scene, scriptNode, protoParent );
			}
			if ( tokenOffset != -1 ) {
			    if ( !v.sameAs( tokenOffset, "}" )) {
        			scriptNode.addChild( new RightBrace( tokenOffset, v ));
        		}
    			scriptNode.setLastTokenOffset( tokenOffset );
    			if ( parentElement instanceof DEFUSENode ) {
    			    parentElement.setLastTokenOffset( tokenOffset );
    			}
    		}
		} catch ( Exception e ) {
			parent.setError( "Could not create script node: " + e.toString() );
			e.printStackTrace();
		}
	}


	/**
	 *  Get the PROTO node parent, null if none.
	 */
	PROTO getPROTOparent( VrmlElement parent ) {
		while ( ! ( parent instanceof PROTO )) {
			parent = parent.getParent();
			if ( parent == null ) {
				break;
			}
		}
		return( (PROTO)parent );
	}
}
