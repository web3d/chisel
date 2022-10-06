/*
 * @(#)MFFloatValue.java
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
import com.trapezium.vrml.Scene;

/**
 *  Scene graph component for sequence of float values.
 *  
 *  Value objects are not added to scene graph, unless they contain errors.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 15 Jan 1998
 *
 *  @since           1.0
 */
public class MFFloatValue extends MFFieldValue {
	boolean nondecreasing = true;
	boolean increasing = true;
	boolean firstTime = true;
	float floatVal = 0f;

	public MFFloatValue() {
		super();
	}

	public boolean isNonDecreasing() {
		return( nondecreasing );
	}

	public boolean isIncreasing() {
		return( increasing );
	}

	public VrmlElement subclassFactory( int tokenOffset, TokenEnumerator v, Scene scene ) {
		if ( !SFFloatValue.valid( tokenOffset, v )) {
			return( new SFFloatValue( tokenOffset, v ));
		} else {
			checkNonDecreasing( tokenOffset, v );
			optimizedValueCount++;
			return( null );
		}
	}

	void checkNonDecreasing( int tokenOffset, TokenEnumerator v ) {
		if ( !nondecreasing ) {
			return;
		} else if ( firstTime ) {
			firstTime = false;
			floatVal = Float.valueOf( v.toString( tokenOffset )).floatValue();
		} else {
			float tempFloatVal = Float.valueOf( v.toString( tokenOffset )).floatValue();
			if ( tempFloatVal < floatVal ) {
				nondecreasing = false;
				increasing = false;
			} else if ( tempFloatVal == floatVal ) {
				increasing = false;
			}
			floatVal = tempFloatVal;
		}
	}
	
	/** Optimization to get the array of floats associated with this field */
    float[] floatArray;
    public float[] getFloatArray() {
        if ( floatArray != null ) {
            return( floatArray );
        }
        TokenEnumerator dataSource = getTokenEnumerator();
        if ( dataSource == null ) {
            return( null );
        }
        dataSource.setState( getFirstTokenOffset() );
        int scanner = dataSource.skipNonNumbers();
        if ( optimizedValueCount > 0 ) {
            floatArray = new float[ optimizedValueCount ];
            for ( int i = 0; i < optimizedValueCount; i++ ) {
                floatArray[i] = dataSource.getFloat( scanner );
                scanner = dataSource.getNextToken();
                scanner = dataSource.skipNonNumbers();
            }
            return( floatArray );
        } else {
            return( null );
        }
    }
}

