/*
 * @(#)NodeRule.java
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
import com.trapezium.vrml.BadFieldId;
import com.trapezium.vrml.ROUTE;
import com.trapezium.util.ReturnInteger;

/**
 *  Creates a PROTO, Script, or built int VRML node scene graph component.
 *
 *  Grammar handled by "Build" method:
 *  <PRE>
 *  node:
 *    nodeTypeId { nodeGuts }
 *    Script { scriptGuts }
 *  </PRE>
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 22 April 1998, added DEFNameFactory option
 *  @version         1.12, 2 April 1998, make class public
 *  @version         1.1, 18 Dec 1997
 *
 *  @since           1.0
 */
public class NodeRule {
    ScriptNodeRule scriptNodeRule;
    BuiltInNodeRule builtInNodeRule;
    PROTONodeRule protoNodeRule;
    DEFNameFactory defNameFactory;
    PROTORule protoRule;
    ROUTERule routeRule;
        
    /** NodeRule constructor
     *
     *  @param defNameFactory if not null, allows for auto-DEF feature, which 
     *     creates DEF names for nodes
     */
    public NodeRule( DEFNameFactory defNameFactory ) {
        this.defNameFactory = defNameFactory;
        scriptNodeRule = new ScriptNodeRule( this );
        builtInNodeRule = new BuiltInNodeRule( this );
        protoNodeRule = new PROTONodeRule( this );
        if ( routeRule == null ) {
            routeRule = new ROUTERule();
        }
        if ( protoRule == null ) {
            protoRule = new PROTORule( this );
        }
    }
    
    /** Get the DEF name factory, returns null if none */
    public DEFNameFactory getDEFNameFactory() {
        return( defNameFactory );
    }
    
    /** Get the associated PROTORule */
    public PROTORule getPROTORule() {
        if ( protoRule == null ) {
            protoRule = new PROTORule( this );
        }
        return( protoRule );
    }
    
    /** Get the associated ROUTERule */
    public ROUTERule getROUTERule() {
        if ( routeRule == null ) {
            routeRule = new ROUTERule();
        }
        return( routeRule );
    }

    
    /** Set the DEF name factory */
    void setDEFNameFactory( DEFNameFactory defNameFactory ) {
        this.defNameFactory = defNameFactory;
        scriptNodeRule.setDEFNameFactory( defNameFactory );
        builtInNodeRule.setDEFNameFactory( defNameFactory );
        protoNodeRule.setDEFNameFactory( defNameFactory );
    }
    
	/**
	 *  Create a single node and add to scene graph.
	 *
	 *  @param tokenOffset   first token of the node
	 *  @param v  TokenEnumerator containing file text
	 *  @param scene  Scene containing the node
	 *  @param parent immediate parent of the node
	 */
	void Build( int tokenOffset, TokenEnumerator v, Scene scene, VrmlElement parent ) {
		GrammarRule.Enter( "NodeRule.Build" );

		if ( v.sameAs( tokenOffset, "Script" )) {
			scriptNodeRule.Build( tokenOffset, v, scene, parent );
		} else {
		    // First identify if the node is a built in type, or a PROTO.  If neither
		    // attempt to get closest spelling match before continuing.
		    String nodeTypeId = v.toString( tokenOffset );
		    if ( VRML97.isBuiltInNode( nodeTypeId )) {
		        builtInNodeRule.Build( nodeTypeId, tokenOffset, v, scene, parent );
		    } else if ( scene.isPROTO( nodeTypeId )) {
		        protoNodeRule.Build( nodeTypeId, tokenOffset, v, scene, parent );
		    } else if ( v.isSpecialCharacter( tokenOffset )) {
		        BadFieldId bfi = new BadFieldId( tokenOffset, "expected node here" );
		        parent.addChild( bfi );
		    } else {
		        // get closest match for built in, then for PROTO, then use the
		        // closest giving preference to built in nodes if there is a match
	            if ( nodeTypeId.compareTo( "ROUTE" ) == 0 ) {
	                ROUTE r = routeRule.Build( tokenOffset, v, scene, parent );
	                r.setError( "ROUTE not allowed" );
	            } else {
    	            BadFieldId bfid = new BadFieldId( tokenOffset );
	                parent.addChild( bfid );
    		        ReturnInteger builtInMatchScore = new ReturnInteger();
    		        ReturnInteger protoMatchScore = new ReturnInteger();
    		        String builtInMatch = VRML97.getClosestNode( nodeTypeId, builtInMatchScore );
    		        String protoMatch = scene.getClosestMatch( nodeTypeId, protoMatchScore );
    	            int biscore = builtInMatchScore.getValue();
    	            int pscore = protoMatchScore.getValue();
    		        if (( builtInMatchScore.getValue() < Spelling.Threshhold( nodeTypeId )) &&
    		            ( protoMatchScore.getValue() < Spelling.Threshhold( nodeTypeId ))) {
        	            bfid.setError( "unknown node or PROTO" );
                    } else if ( builtInMatchScore.getValue() >= protoMatchScore.getValue() ) {
                        bfid.setError( "unknown node or PROTO, possibly '" + builtInMatch + "'" );
                        builtInNodeRule.Build( builtInMatch, tokenOffset, v, scene, parent );
                    } else {
                        bfid.setError( "unknown node or PROTO, possibly PROTO '" + protoMatch + "'" );
                        protoNodeRule.Build( protoMatch, tokenOffset, v, scene, parent );
                    }
                }
            }
        }
        GrammarRule.Exit( "NodeRule.Build" );
    }
}
