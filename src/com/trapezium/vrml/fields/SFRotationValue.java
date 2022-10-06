/*
 * @(#)SFRotationValue.java
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
import com.trapezium.vrml.VrmlElement;

/**
 *  Scene graph component for an SFRotation field value.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 8 Dec 1997
 *
 *  @since           1.0
 */
public class SFRotationValue extends SFFieldValue {
	/** The meaning of each child offset */
	static public final int X = 0;
	static public final int Y = 1;
	static public final int Z = 2;
	static public final int ANGLE = 3;

	int validChildCount = 0;

	public SFRotationValue() {
		super();
	}

	public SFRotationValue( int tokenOffset, TokenEnumerator v ) {
		super( tokenOffset );
		init( tokenOffset, v, null );
	}

	public int numberValidChildren() {
		return( validChildCount );
	}


	public void init( int tokenOffset, TokenEnumerator v, Scene scene ) {
		SFFloatValue.validateAndAdd( this, tokenOffset, v );
		if ( getChildAt( 0 ) == null ) {
			validChildCount++;
		} else if ( !v.isNumber( tokenOffset )) {
			return;
		}
		int state = v.getState();
		tokenOffset = v.getNextToken();
		SFFloatValue.validateAndAdd( this, tokenOffset, v );
		VrmlElement vcheck = getChildAt( 1 - validChildCount );
		if ( vcheck == null ) {
			validChildCount++;
		} else if ( bracketError( vcheck, v ) || !v.isNumber( tokenOffset )) {
			v.setState( state );
			return;
		}
		state = v.getState();
		tokenOffset = v.getNextToken();
		SFFloatValue.validateAndAdd( this, tokenOffset, v );
		vcheck = getChildAt( 2 - validChildCount );
		if ( vcheck == null ) {
			validChildCount++;
		} else if ( bracketError( vcheck, v ) || !v.isNumber( tokenOffset )) {
			v.setState( state );
			return;
		}
		state = v.getState();
		tokenOffset = v.getNextToken();
		SFFloatValue.validateAndAdd( this, tokenOffset, v );
		vcheck = getChildAt( 3 - validChildCount );
		if ( vcheck == null ) {
			validChildCount++;
		} else if ( bracketError( vcheck, v ) || !v.isNumber( tokenOffset )) {
			v.setState( state );
		}
	}


	static public boolean valid( int tokenOffset, TokenEnumerator v ) {
		if ( !SFFloatValue.valid( tokenOffset, v )) {
			return( false );
		}
		int state = v.getState();
		for ( int i = 0; i < 3; i++ ) {
			tokenOffset = v.getNextToken();
			if ( !SFFloatValue.valid( tokenOffset, v )) {
				v.setState( state );
				return( false );
			}
		}
		return( true );
	}
}
