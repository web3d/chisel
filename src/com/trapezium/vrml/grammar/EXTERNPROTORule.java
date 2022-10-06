/*
 * @(#)EXTERNPROTORule.java
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
import com.trapezium.vrml.Scene;
import com.trapezium.vrml.NodeTypeId;
import com.trapezium.vrml.LeftBracket;
import com.trapezium.vrml.RightBracket;
import com.trapezium.vrml.node.EXTERNPROTO;
import com.trapezium.vrml.node.PROTObase;
import com.trapezium.vrml.fields.MFStringValue;

/**
 *  Creates an EXTERNPROTO scene graph component.
 *  
 *  Grammar handled by "Build" method:
 *  <PRE>
 *  proto:
 *    EXTERNPROTO nodeTypeId [ externInterfaceDeclarations ] mfstringValue
 *  </PRE>    
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 19 Jan 1998
 *
 *  @since           1.0
 */
class EXTERNPROTORule {
    ExternInterfaceDeclarationRule externInterfaceDeclarationRule;
    
    /** class constructor */
    EXTERNPROTORule( NodeRule nodeRule ) {
        externInterfaceDeclarationRule = new ExternInterfaceDeclarationRule( nodeRule, false );
    }
    
    /** Create an EXTERNPROTO declaration and add to scene graph.
     *
     *  @param  tokenOffset  first token of the EXTERNPROTO declaration
     *  @param  v  TokenEnumerator containing file text
     *  @param  scene  Scene containing the EXTERNPROTO
     *  @param  parent immediate parent of the EXTERNPROTO
     */
	void Build( int tokenOffset, TokenEnumerator v, Scene scene, VrmlElement parent ) {
		GrammarRule.Enter( "EXTERNPROTORule.Build" );

		// at this point, tok is known to be "EXTERNPROTO", get next one
		int originalTokenOffset = tokenOffset;
		tokenOffset = v.getNextToken();
		if ( tokenOffset == -1 ) {
			return;
		} else {
			EXTERNPROTO p = new EXTERNPROTO( originalTokenOffset );
			NodeTypeId ntid = new NodeTypeId( tokenOffset, v );
			p.addChild( ntid );
			tokenOffset = v.getNextToken();
			boolean firstTimeSkip = false;
   			LeftBracket lb = new LeftBracket( tokenOffset, v );
   			p.addChild( lb );
   			if ( lb.getError() != null ) {
    			firstTimeSkip = true;
    		}
			while ( true ) {
				if ( !firstTimeSkip ) {
					tokenOffset = v.getNextToken();
				}
				firstTimeSkip = false;
				if ( tokenOffset == -1 ) {
					parent.setError( "No matching right bracket" );
					break;
				} else if ( v.sameAs( tokenOffset, "]" )) {
					break;
				}
				externInterfaceDeclarationRule.Build( tokenOffset, v, scene, p );
			}
   			p.addChild( new RightBracket( tokenOffset, v ));
   			MFStringValue mfsv = new MFStringValue( v.getNextToken(), v );
   			p.setFieldValue( mfsv );
   			p.setLastTokenOffset( mfsv.getLastTokenOffset() );
//			p.addChild( new MFStringValue( v.getNextToken(), v ));

			parent.addChild( p );
			PROTObase pb = scene.getPROTO( p.getId() );
			if ( pb != null ) {
				ntid.setError( "Warning, there is another PROTO with this name" );
			}
			scene.registerPROTO( p );
		}
		GrammarRule.Exit( "EXTERNPROTORule.Build" );
	}
}
