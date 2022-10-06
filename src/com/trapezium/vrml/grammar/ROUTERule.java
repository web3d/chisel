/*
 * @(#)ROUTERule.java
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
import com.trapezium.vrml.ROUTE;
import com.trapezium.vrml.TO;
import com.trapezium.vrml.RouteSource;
import com.trapezium.vrml.RouteDestination;
import com.trapezium.vrml.node.DEFUSENode;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.fields.Field;

/**
 *  Creates the scene graph component for a ROUTE.
 *  
 *  Grammar handled by "Build" method:
 *  <PRE>
 *    ROUTE nodeNameId.eventOutId TO nodeNameId.eventInId
 *  </PRE>    
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 12 Dec 1997
 *
 *  @since           1.0
 */
class ROUTERule {
    /** Create a ROUTE and add it to scene graph.
     *
     *  @param tokenOffset   first token of the ROUTE
     *  @param v             token enumerator containing file text
     *  @param scene         scene containing the ROUTE
     *  @param parent        immediate parent of the ROUTE
     */
	ROUTE Build( int tokenOffset, TokenEnumerator v, Scene scene, VrmlElement parent ) {
		GrammarRule.Enter( "ROUTERule.Build" );

		ROUTE r = new ROUTE( tokenOffset, v );

		// at this point, tok is known to be "ROUTE", get next one
		RouteSource rs = new RouteSource( v.getNextToken(), v, scene );
		r.addChild( rs );
		r.addChild( new TO( v.getNextToken(), v ));
		RouteDestination rd = new RouteDestination( v.getNextToken(), v, scene );
		r.addChild( rd );
		parent.addChild( r );
		r.checkTypes();
		String sourceObjectName = r.getSourceDEFname();
		if ( sourceObjectName != null ) {
			DEFUSENode dun = scene.getDEF( sourceObjectName );
			if ( dun == null ) {
				rs.setError( "DEF '" + sourceObjectName + "' not found" );
			} else {
				dun.markUsed();
				dun.markUsedByROUTE();
			}
		}
		String destObjectName = r.getDestDEFname();
		if ( destObjectName != null ) {
			DEFUSENode dun = scene.getDEF( destObjectName );
			if ( dun == null ) {
				rd.setError( "DEF '" + destObjectName + "' not found" );
			} else {
				dun.markUsed();
				dun.markUsedByROUTE();
				Node n = dun.getNode();
				if ( n == null ) {
				//    System.out.println( "check it out" );
				} else if ( n.getBaseName() == null ) {
				 //   System.out.println( "check it out" );
				} else if ( n.getBaseName().compareTo( "Script" ) == 0 ) {
					Field url = n.getField( "url" );
					if ( url != null ) {
						if ( javascriptEmbedded( url.getFirstTokenOffset(), v )) {
							VRML97.checkScript( n, r, rd, v );
						}
					}
				}
			}
		}
		if ( scene.hasRoute( sourceObjectName, r.getSourceFieldName(), destObjectName, r.getDestFieldName() )) {
		    r.setError( "repeated ROUTE" );
		} else {
		    scene.addRoute( sourceObjectName, r.getSourceFieldName(), destObjectName, r.getDestFieldName() );
		}
		r.setLastTokenOffset( rd.getLastTokenOffset() );
		if ( rd.getLastTokenOffset() != -1 ) {
    		v.breakLineAt( rd.getLastTokenOffset() + 1 );
    	}
		GrammarRule.Exit( "ROUTERule.Build" );
		return( r );
	}



	boolean javascriptEmbedded( int tokenOffset, TokenEnumerator v ) {
		if ( tokenOffset != -1 ) {
		    int oldState = v.getState();
		    v.setState( tokenOffset );
			tokenOffset = v.getNextToken();
			v.setState( oldState );
			if ( tokenOffset != -1 ) {
				String testString = v.toString( tokenOffset );
				if ( testString.indexOf( "javascript:" ) > 0 ) {
					return( true );
				} else if ( testString.indexOf( "vrmlscript:" ) > 0 ) {
					return( true );
				}
			}
		}
		return( false );
	}
}
