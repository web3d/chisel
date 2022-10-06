/*
 * @(#)Translator.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.node;

import com.trapezium.parse.TokenEnumerator;
import com.trapezium.vrml.fields.MFFieldValue;
import com.trapezium.vrml.Value;

/**
 *  Converts an index field offset into an offset into a value node.
 *
 *  This is used to convert IndexedFaceSet index field offsets into
 *  corresponding color/normal/texture values.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 9 March 1998, rewrote for faster parsing of 
 *                   IndexedFaceSets with index fields
 *  @version         1.1, 9 Dec 1997
 *
 *  @since           1.0
 */
public class Translator {
    /** the MFInt32 field holding the <B>index</B> */
	MFFieldValue field;
	
	/** the number of values in that <B>index</B> field */
	int numberValues;
	
	/** the token offsets for each number */
	int[] tokenOffsets;

	public Translator( TokenEnumerator v, MFFieldValue field, int numberValues ) {
		this.field = field;
		this.numberValues = numberValues;
		if ( numberValues > 0 ) {
		    tokenOffsets = new int[ numberValues ];
		    int scanner = field.getFirstTokenOffset();
		    int endTokenOffset = field.getLastTokenOffset();
		    int numberIdx = 0;
		    for ( int i = 0; i < numberValues; i++ ) {
		        tokenOffsets[i] = -1;
		    }
		    int oldState = v.getState();
		    v.setState( scanner );
		    while ( true ) {
		        if ( v.isNumber( scanner )) {
		            if ( numberIdx < numberValues ) {
		                tokenOffsets[ numberIdx++ ] = scanner;
		            }
		        }
		        scanner = v.getNextToken();
		        if ( scanner >= endTokenOffset ) {
		            break;
		        }
		    }
		    v.setState( oldState );
		} else {
		    tokenOffsets = null;
		}
	}

    /**
     *  Convert an <B>index</B> offset into an offset into the corresponding <B>value</B> array.
     *  For example, an IndexedFaceSet colorIndex offset is converted into the
     *  value at that offset.  However, that value is checked against the maximum possible
     *  offset in the value list, and if it is greater, the scene graph component is
     *  marked with an error.  If the end of the index list is reached before
     *  the specified offset is found, an error is also logged.
     *
     *  @param  indexOffset  the offset into the <B>index</B> list
     *  @param  maxval  the maximum offset allowed in the <B>value</B> list
     *  @param  nodeToBeVerified  if there are any errors, they are added as children of this node
     *  @param  indexName  the index name used as part of the error message
     *            if any error occurs.  Possible values are "colorIndex", 
     *            "coordIndex", "normalIndex", or "texCoordIndex"
     *
     *  @return  the offset into the value list, or -1 if no such offset can be found
     */
    public int getOffset( int indexOffset, int maxval, Node nodeToBeVerified, int indicatorOffset, TokenEnumerator v, String indexName ) {
		int scannerVal = -1;
		int scannerOffset = -1;
		if ( numberValues == 0 ) {
			return( indexOffset );
		} else if ( indexOffset < numberValues ) {
			scannerOffset = tokenOffsets[ indexOffset ];
	    }
	    if ( scannerOffset != -1 ) {
	        scannerVal = v.getIntValue( scannerOffset );
	    }
	    if (( scannerVal != -1 ) && ( scannerVal < maxval )) {
	        return( scannerVal );
	    }

		Value val = new Value( indicatorOffset );
		if ( scannerVal == -1 ) {
			val.setError( "face " + indexOffset + " has no corresponding " + indexName + " value" );
		} else {
			val.setError( "face " + indexOffset + " " + indexName + " value is " + scannerVal + ", max value is " + (maxval-1) );
		}
		nodeToBeVerified.addChild( val );
		return( -1 );
	}
}

