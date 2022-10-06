/*
 * @(#)SFInt32Value.java
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
import com.trapezium.vrml.Scene;
import com.trapezium.vrml.Value;
import com.trapezium.vrml.ErrorElement;

/**
 *  Scene graph component for an SFInt32 field value.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 15 Jan 1998
 *
 *  @since           1.0
 */
public class SFInt32Value extends SFFieldValue {
    int intValue;
    
	public SFInt32Value() {
		super();
	}

	public SFInt32Value( int tokenOffset, TokenEnumerator v ) {
		super( tokenOffset );
		init( tokenOffset, v, null );
	}

	public void init( int tokenOffset, TokenEnumerator v, Scene scene ) {
	    if ( !valid( tokenOffset, v )) {
	        addChild( new ErrorElement( tokenOffset, "invalid integer" ));
	    } else {
    		addChild( new Value( tokenOffset ));
    		intValue = v.getIntValue( tokenOffset );
    	}
	}

    public int getIntValue() {
        return( intValue );
    }
    
	//
	//  for vrmllint, MFFieldValue int children only created if token is valid,
	//  otherwise we don't create children ... maybe optimization at level of
	//  tokens would be better, i.e. along the lines of not creating new tokens,
	//  but embedding coded forms of standard "next" tokens in the existing set,
	//  for handling numbers, etc.
	//
	static public boolean valid( int tokenOffset, TokenEnumerator v ) {
		if ( tokenOffset == -1 ) {
			return( false );
		}
		int idx = 0;
		int vlen = v.length();
		if ( v.charAt( idx ) == '-' ) {
			idx++;
			vlen--;
		} else if ( v.charAt( idx ) == '+' ) {
			idx++;
			vlen--;
		}
		for ( int i = idx; i < vlen; i++ ) {
			if ( !Character.isDigit( v.charAt( idx ))) {
				return( false );
			}
		}
		return( true );
	}
}

