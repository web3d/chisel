/*
 * @(#)SFVec2fValue.java
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

/**
 *  Scene graph component for an SFVec2f field value.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 18 Nov 1997
 *
 *  @since           1.0
 */
public class SFVec2fValue extends SFFieldValue {
	public SFVec2fValue() {
		super();
	}

	public SFVec2fValue( int tokenOffset, TokenEnumerator v ) {
		super( tokenOffset );
		init( tokenOffset, v, null );
	}


    int validChildCount = 0;
	public void init( int tokenOffset, TokenEnumerator v, Scene scene ) {
		SFFloatValue.validateAndAdd( this, tokenOffset, v );
		if ( !v.isNumber( tokenOffset )) {
			return;
		}
		validChildCount++;
		int state = v.getState();
		tokenOffset = v.getNextToken();
		SFFloatValue.validateAndAdd( this, tokenOffset, v );
		if ( !v.isNumber( tokenOffset )) {
			v.setState( state );
		} else {
		    validChildCount++;
		}
	}

	public int numberValidChildren() {
		return( validChildCount );
	}

	static public boolean valid( int tokenOffset, TokenEnumerator v ) {
		if ( !SFFloatValue.valid( tokenOffset, v )) {
			return( false );
		}
		int state = v.getState();
		if ( !SFFloatValue.valid( v.getNextToken(), v )) {
			v.setState( state );
			return( false );
		}
		return( true );
	}
}
