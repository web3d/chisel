/*
 * @(#)SFStringValue.java
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
import com.trapezium.parse.TokenTypes;
import com.trapezium.vrml.Scene;
import com.trapezium.vrml.Value;
import com.trapezium.vrml.grammar.Table7;

/**
 *  Scene graph component for an SFString field value.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 18 Feb 1998, Table 7 base profile warning
 *  @version         1.1, 8 Dec 1997
 *
 *  @since           1.0
 */
public class SFStringValue extends SFFieldValue implements TokenTypes {
	public SFStringValue() {
		super();
	}

	public SFStringValue( int tokenOffset, TokenEnumerator v ) {
		super( tokenOffset );
		init( tokenOffset, v, null );
	}

	public void init( int tokenOffset, TokenEnumerator v, Scene scene ) {
	    // "-2 because length includes quotes, limit does not include quotes
	    int len = v.length( tokenOffset ) - 2;
	    Value val = new Value( tokenOffset );
	    if ( v.getType( tokenOffset ) != QuotedString ) {
	        val.setError( "Expected quoted string" );
	    }
	    int state = v.getState();
	    tokenOffset = v.getNextToken();
	    if ( tokenOffset != -1 ) {
    	    while ( v.getType( tokenOffset ) == QuotedStringContinuation ) {
    	        state = v.getState();
    	        len += v.length( tokenOffset );
    	        tokenOffset = v.getNextToken();
    	        if ( tokenOffset == -1 ) {
    	            break;
    	        }
    	    }
    	}
	    v.setState( state );
	    if ( len > Table7.SFStringLengthLimit ) {
	        val.setError( "Nonconformance, length " + len + " exceeds base profile SFString length limit " + Table7.SFStringLengthLimit );
	    }
		addChild( val );
	}
	
	/** Get the String value without quotes */
	public String getStringValue() {
	    Scene scene = (Scene)getRoot();
	    if ( scene != null ) {
	        TokenEnumerator te = scene.getTokenEnumerator();
	        if ( te != null ) {
	            String s = te.toString( getFirstTokenOffset() );
	            if (( s.charAt( 0 ) == '"' ) && ( s.charAt( s.length() - 1 ) == '"' )) {
	                s = s.substring( 1, s.length() - 1 );
	            }
	            return( s );
	        }
	    }
	    return( null );
	}
}
