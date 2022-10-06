/*
 * @(#)PROTORule.java
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
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.node.PROTO;
import com.trapezium.vrml.node.PROTObase;
import com.trapezium.vrml.Scene;
import com.trapezium.vrml.LeftBrace;
import com.trapezium.vrml.LeftBracket;
import com.trapezium.vrml.RightBrace;
import com.trapezium.vrml.RightBracket;
import com.trapezium.vrml.NodeTypeId;

/**
 *  Creates the scene graph component for a PROTO declaration.
 *  
 *  Grammar handled by "Build" method:
 *  <PRE>
 *   proto ::=
 *        PROTO nodeTypeId [ interfaceDeclarations ] { protoBody } ;
 *           
 *   interfaceDeclarations ::=
 *        interfaceDeclaration |
 *        interfaceDeclaration interfaceDeclarations |
 *             empty ;
 *
 *   protoBody ::=
 *        protoStatements node statements ;
 *
 *   protoStatements ::=
 *        protoStatement |
 *        protoStatement protoStatements |
 *        empty ;
 *  </PRE>
 *  Note:  the implementation of the "protoBody" portion of above grammar
 *  is done through the SceneRule, followed by a check that the body 
 *  consists of at least one node.  This check (and possible error mark)
 *  is done by the "setBuiltInNodeType" method.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 6 Jan 1998
 *
 *  @since           1.0
 */
public class PROTORule {
    int level = 0;
    InterfaceDeclarationRule interfaceDeclarationRule;
    
    /** Class constructor */
    PROTORule( NodeRule nodeRule ) {
        interfaceDeclarationRule = new InterfaceDeclarationRule( nodeRule );
    }
        
	void Build( int tokenOffset, TokenEnumerator v, Scene scene, VrmlElement parent ) {
		GrammarRule.Enter( "PROTORule.Build" );
	    level++;
		// at this point, tok is known to be "PROTO", get next one
		int originalTokenOffset = tokenOffset;
		tokenOffset = v.getNextToken();
		if ( tokenOffset != -1 ) {
			PROTO p = new PROTO( originalTokenOffset );
			NodeTypeId ntid = new NodeTypeId( tokenOffset, v );
			p.addChild( ntid );
			tokenOffset = v.getNextToken();
			boolean leftBracketMissing = false;
			if ( !v.sameAs( tokenOffset, "[" )) {
			    // if there was no left bracket, assume it was missing,
			    // continue as if it was there
                p.addChild( new LeftBracket( tokenOffset, v ));
                leftBracketMissing = true;
            }
			while ( true ) {
				if ( !leftBracketMissing ) {
					tokenOffset = v.getNextToken();
				}
				leftBracketMissing = false;
				if ( tokenOffset == -1 ) {
					p.setError( "Did not find matching right bracket" );
					break;
				} else if ( v.sameAs( tokenOffset, "]" )) {
					break;
				}
				interfaceDeclarationRule.Build( tokenOffset, v, scene, p );
			}
			if ( !v.sameAs( tokenOffset, "]" )) {
    			p.addChild( new RightBracket( tokenOffset, v ));
    		}
			Scene s = new Scene();
			s.setErrorSummary( scene.getErrorSummary() );
			s.setTokenEnumerator( v );
			s.setPROTOparent( p );
			p.addChild( s );

			p.addChild( new LeftBrace( v.getNextToken(), v ));

			parent.addChild( p );

			// Build the scene associated with the PROTO
			VRML97parser parser = VRML97parser.singleton;
			parser.Build( v, s, s, "}" );

            if ( v.getCurrentTokenOffset() != -1 ) {
    			p.addChild( new RightBrace( v.getCurrentTokenOffset(), v ));
    		}
    		p.setLastTokenOffset( v.getCurrentTokenOffset() );

			// Set the type of the PROTO node based on the first node defined in the scene,
			// then register the prototype with the scene
			p.setBuiltInNodeType( s.getFirstNodeType() );
			PROTObase pb = scene.getPROTO( p.getId() );
			if ( pb != null ) {
				ntid.setError( "Warning, there is another PROTO with this name" );
			}
			
			//  Register the PROTO declaration with the scene. 
			scene.registerPROTO( p );
			
			//  Look for unused fields from the PROTO interface declaration
			p.checkInUse();
    		if ( level == ( Table7.PROTONestingLimit + 1 )) {
                p.setError( "Nonconformance, base profile PROTO nesting limit " + Table7.PROTONestingLimit + " exceeded here" );
            }
		} else {
			parent.setError( "Unexpected end of file" );
		}
		level--;
		GrammarRule.Exit( "PROTORule.Build" );
	}
}
