/*
 * @(#)SFFieldValue.java
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
import com.trapezium.vrml.VrmlElement;

/**
 *  Base class for all single value field values.
 *
 *  Multiple (i.e. variable number of values) value field values represented
 *  by base class MFFieldValue.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 17 Dec 1997
 *
 *  @since           1.0
 */
abstract public class SFFieldValue extends FieldValue {
	public SFFieldValue() {
		super();
	}

	public SFFieldValue( int tokenOffset ) {
		super( tokenOffset );
	}

	public int numberValidChildren() {
		int validCount = 0;
		for ( int i = 0; i < numberChildren(); i++ ) {
			VrmlElement vle = getChildAt( i );
			if ( vle.getError() == null ) {
				validCount++;
			}
		}
		return( validCount );
	}
	
	boolean bracketError( VrmlElement checkElement, TokenEnumerator v ) {
		if ( checkElement == null ) {
			return( true );
		}
		if ( checkElement.getError() != null ) {
			int tokenOffset = checkElement.getFirstTokenOffset();
			if ( tokenOffset != -1 ) {
				return( v.isRightBracket( tokenOffset ) || v.isRightBrace( tokenOffset ));
			}
		}
		return( false );
	}
}









