/*
 * @(#)GrammarRule.java
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
//import com.trapezium.vrml.FieldTip;
import com.trapezium.vrml.node.NodeType;
import com.trapezium.parse.TokenEnumerator;

/**
 *  Grammar rule entry/exit debugging prints.
 *  
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 12 Nov 1997
 *
 *  @since           1.0
 */
class GrammarRule {
	static public boolean debug = false;
	static public int indentation = 0;
	static public String spacer = "                                                                      ";

	static void Enter( String s ) {
		if ( debug ) {
			indentation++;
			Debug( s + " enter" );
		}
	}

	static void Exit( String s ) {
		if ( debug ) {
			Debug( s + " exit" );
			indentation--;
		}
	}

	static void Debug( String s ) {
		if ( debug ) {
			// indentation can go below zero depending on where we turn on debugging
			if ( indentation > 0 ) {
				System.out.println( spacer.substring( 0, indentation ) + s );
			} else {
				System.out.println( s );
			}
		}
	}

	static boolean additionalInfo( TokenEnumerator v ) {
		int state = v.getState();
		int tokenOffset = v.getNextToken();
		if ( v.sameAs( tokenOffset, "tip" )) {
			v.setState( state );
			return( true );
		} else if ( v.sameAs( tokenOffset, "nodeType" )) {
			v.setState( state );
			return( true );
		} else if ( v.sameAs( tokenOffset, "actualType" )) {
			v.setState( state );
			return( true );
		} else {
			v.setState( state );
			return( false );
		}
	}

	static VrmlElement Factory( TokenEnumerator v ) {
//		Token tok = v.nextToken();
//		return( Factory( tok, v ));
return( null );
	}

	static VrmlElement Factory( int tokenOffset, TokenEnumerator v ) {
//		if ( tok.sameAs( "tip" )) {
//			return( new FieldTip( v ));
//		} else 
//if ( tok.sameAs( "nodeType" )) {
//			tok = v.nextToken();
//			return( new NodeType( tok ));
//		} else {
			return( null );
//		}
	}
}
