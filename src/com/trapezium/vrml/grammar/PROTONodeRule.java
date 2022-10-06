/*
 * @(#)PROTONodeRule.java
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
import com.trapezium.vrml.node.PROTOInstance;
import com.trapezium.vrml.node.NodeType;
import com.trapezium.vrml.node.DEFUSENode;

/**
 *  Creates the scene graph component for a PROTO instance.
 *
 *  Grammar handled by "Build" method:
 *  <PRE>
 *    protoNodeTypeId { nodeGuts }
 *  </PRE>
 *  Note:  the PROTOInstance "copyBaseNodeInfo" method does not copy
 *  all nodes in the PROTO declaration body, and substitute values as
 *  indicated by IS.  When this is done, PROTO checking can be done
 *  for PROTO interface fields passed through IS.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 24 April 1998, added autoDEF
 *  @version         1.12, 6 Mar 1998, added Table 7 limits
 *  @version         1.1, 6 Jan 1998
 *
 *  @since           1.0
 */
class PROTONodeRule extends BuiltInNodeRule {
    DEFNameFactory defNameFactory;

    /** class constructor */
    PROTONodeRule( NodeRule nodeRule ) {
        super( nodeRule );
        defNameFactory = nodeRule.getDEFNameFactory();
    }

    /** Set DEFNameFactory, used when scene factory overrides parser factory */
    void setDEFNameFactory( DEFNameFactory defNameFactory ) {
        this.defNameFactory = defNameFactory;
    }

    /**
     *  Create a PROTO instance and add it to the scene graph.
     *
     *  @param  nodeType     type of PROTO to create
     *  @param  tokenOffset  first token of the PROTO instance
     *  @param  v            contains text of VRML file
     *  @param  scene        scene containing PROTO
     *  @param  parent       immediate parent of PROTO
     */
	void Build( String nodeType, int tokenOffset, TokenEnumerator v, Scene scene, VrmlElement parent ) {
		GrammarRule.Enter( "PROTONodeRule.Build" );
		try {
    		PROTOInstance pi = scene.PROTOFactory( nodeType );

    		// three conditions required for auto DEFfing:
    		// 1. defNameFactory non-null
    		// 2. parent not already a DEF
    		// 3. defNameFactory returns non-null
    		VrmlElement parentElement = parent;
    		if (( defNameFactory != null ) && !( parent instanceof DEFUSENode )) {
    		    String defName = defNameFactory.createDEFName( pi.getPROTOname() );
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

    		pi.setFirstTokenOffset( tokenOffset );
    		parentElement.addChild( pi );
    		BuildNodeGuts( pi, tokenOffset, v, scene );
    		pi.copyBaseNodeInfo();
    		pi.verify( v );
            if ( parentElement instanceof DEFUSENode ) {
                parentElement.setLastTokenOffset( pi.getLastTokenOffset() );
            }
            pi.checkInUse();
    	} catch ( Exception e ) {
    	    System.out.println( "Exception " + e );
    	    e.printStackTrace();
    	}
        GrammarRule.Exit( "PROTONodeRule.Build" );
    }
}
